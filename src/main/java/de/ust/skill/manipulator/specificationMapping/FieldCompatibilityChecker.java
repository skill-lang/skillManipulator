package de.ust.skill.manipulator.specificationMapping;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import de.ust.skill.common.java.internal.FieldDeclaration;
import de.ust.skill.common.java.internal.FieldType;
import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.common.java.internal.StoragePool;
import de.ust.skill.common.java.internal.fieldTypes.ConstantLengthArray;
import de.ust.skill.common.java.internal.fieldTypes.FloatType;
import de.ust.skill.common.java.internal.fieldTypes.IntegerType;
import de.ust.skill.common.java.internal.fieldTypes.MapType;
import de.ust.skill.common.java.internal.fieldTypes.SingleArgumentType;
import de.ust.skill.manipulator.specificationMapping.messages.FieldMappingInformation;

/**
 * This class provides a function to check the compatibility of two fields.
 * 
 * @author olibroe
 *
 */
public class FieldCompatibilityChecker {
	
	// possible results of the static check
	protected enum TypeRelation { COMPATIBLE, DYN_CHECK_NEEDED, NOT_COMPATIBLE };
	
	// corresponding SpecificationMapper, needed for usertype checks
	private SpecificationMapper specificationMapper;
	
	public FieldCompatibilityChecker(SpecificationMapper specificationMapper) {
		this.specificationMapper = specificationMapper;
	}
	
	/**
	 * The compatibility is checked in two steps:
	 * 1. Static Check: Check the static compatibility of field types. 
	 * 2. Dynamic Check: Only needed when the static check can not decide.
	 * 					 Then all objects of the old field are checked for compatibility with the new field type.
	 * 
	 * @param oldField - field of the old typesystem
	 * @param newField - field of the new typesystem
	 * @param oldPool - type which object need to be checked in dynamic check
	 * @return
	 */
	public boolean fieldsCompatible(FieldDeclaration<?, ?> oldField, FieldDeclaration<?, ?> newField,
			StoragePool<?,?> oldPool) {
		
		// get the right checks
		Check check = dispatchField(newField.type());
		
		// 1.static check
		TypeRelation tr = check.staticCheck(oldField.type());
		
		if(tr == TypeRelation.NOT_COMPATIBLE) return false;

		// 2.dynamic check if needed
		if(tr == TypeRelation.DYN_CHECK_NEEDED) {
			for(SkillObject o : oldPool) {
				if(!check.dynamicCheck(oldField.get(o))) return false;
			}
		}
		
		// warning for float -> int mappings
		if(oldField.type() instanceof FloatType<?> && newField.type() instanceof IntegerType) {
			specificationMapper.addToMappingLog(new FieldMappingInformation(oldField, newField,
					"Mapping from floating point type to integer type may lead to loss of precision"));
		}

		// warning for int -> float mappings
		if(oldField.type() instanceof IntegerType && newField.type() instanceof FloatType<?>) {
			specificationMapper.addToMappingLog(new FieldMappingInformation(oldField, newField,
					"Mapping from integer type to floating point type may lead to loss of precision"));
		}
		
		return  true;
	}

	/**
	 * Every type has its own implementation of checks. This function gives the right checks for the given
	 * field type. The returned Check has implemented checks for cast ON newType.
	 * 
	 * @param newType - type for which we need checks
	 * @return instance of Check interface
	 */
	private Check dispatchField(FieldType<?> newType) {
		int typeID = newType.typeID;
		
		switch(typeID) {
		case 5: return annotationCheck;
		case 6: return boolCheck;
		case 7: return byteCheck;
		case 8: return shortCheck;
		case 9: return intCheck;
		case 10:
		case 11:
			return longCheck;
		case 12: return floatCheck;
		case 13: return doubleCheck;
		case 14: return stringCheck;
		case 15: return new ConstantLengthArrayCheck(newType);
		case 17:
		case 18:
		case 19:
			return new VariableLengthCollectionCheck(newType);
		case 20:
			return new MapCheck(newType);
		default:
			return new UsertypeCheck(newType);
		}
	}
	
	// this checks are not dependent on new type and therefore can be initialized at the beginning
	private final AnnotationCheck annotationCheck = new AnnotationCheck();
	private final BoolCheck boolCheck = new BoolCheck();
	private final I8Check byteCheck = new I8Check();
	private final I16Check shortCheck = new I16Check();
	private final I32Check intCheck = new I32Check();
	private final I64Check longCheck = new I64Check();
	private final F32Check floatCheck = new F32Check();
	private final F64Check doubleCheck = new F64Check();
	private final StringCheck stringCheck = new StringCheck();
	
	/**
	 * This is the abstract definition of a check interface.
	 * It provides a static and a dynamic check method.
	 * 
	 * @author olibroe
	 *
	 */
	private abstract class Check {	
		abstract TypeRelation staticCheck(FieldType<?> oldType);
		abstract boolean dynamicCheck(Object o);
	}
	
	/**
	 * Checks for cast on annotation type.
	 * @author olibroe
	 *
	 */
	private class AnnotationCheck extends Check {
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			// old type can be annotation(=5) itself or usertype(>=32) to be compatible 
			if(oldType.typeID == 5 || oldType.typeID >= 32) return TypeRelation.COMPATIBLE;
			return TypeRelation.NOT_COMPATIBLE;
		}

		@Override
		boolean dynamicCheck(Object o) {
			// not needed for cast on annotation
			return true;
		}

	}
	
	/**
	 * Checks for cast on bool type.
	 * @author olibroe
	 *
	 */
	private class BoolCheck extends Check {
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			// only compatible with bool type itself
			if(oldType.typeID == 6) return TypeRelation.COMPATIBLE;
			return TypeRelation.NOT_COMPATIBLE;
		}

		@Override
		boolean dynamicCheck(Object o) {
			// not needed for cast on bool
			return true;
		}

	}
	
	/**
	 * Checks for cast on i8 type.
	 * @author olibroe
	 *
	 */
	private class I8Check extends Check {
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			int id = oldType.typeID;
			// compatible with itself
			if(id == 7) return TypeRelation.COMPATIBLE;
			// dynamic check needed for bigger int types i16,i32,i64,v64 and float types f32,f64
			if(8 <= id && id <= 13) return TypeRelation.DYN_CHECK_NEEDED;
			return TypeRelation.NOT_COMPATIBLE;
		}

		@Override
		boolean dynamicCheck(Object o) {
			// Object o can be either Long, Int, Short or Float/Double according to static check
			// int and short can be converted lossless to double
			if(o instanceof Long) {
				long value = ((Number)o).longValue();
				return Byte.MIN_VALUE <= value && value <= Byte.MAX_VALUE; 
			} else {
				double value = ((Number)o).doubleValue();
				if(Double.isNaN(value)) return false;
				return Byte.MIN_VALUE <= value && value <= Byte.MAX_VALUE; 
			}
		}

	}
	
	/**
	 * Checks for cast on i16 type.
	 * @author olibroe
	 *
	 */
	private class I16Check extends Check {
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			int id = oldType.typeID;
			// compatible with itself and i8
			if(7 <= id && id <= 8) return TypeRelation.COMPATIBLE;
			// dynamic check needed for bigger int types i32,i64,v64 and float types f32,f64
			if(9 <= id && id <= 13) return TypeRelation.DYN_CHECK_NEEDED;
			return TypeRelation.NOT_COMPATIBLE;
		}

		@Override
		boolean dynamicCheck(Object o) {
			// Object o can be either Long, Int or Float/Double
			// int can be converted lossless to double
			if(o instanceof Long) {
				long value = ((Number)o).longValue();
				return Short.MIN_VALUE <= value && value <= Short.MAX_VALUE; 
			} else {
				double value = ((Number)o).doubleValue();
				if(Double.isNaN(value)) return false;
				return Short.MIN_VALUE <= value && value <= Short.MAX_VALUE; 
			}
		}

	}
	
	/**
	 * Checks for cast on i32 type.
	 * @author olibroe
	 *
	 */
	private class I32Check extends Check {
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			int id = oldType.typeID;
			// compatible with itself and i8,i16
			if(7 <= id && id <= 9) return TypeRelation.COMPATIBLE;
			// dynamic check needed for bigger int types i64,v64 and float types f32,f64
			if(10 <= id && id <= 13) return TypeRelation.DYN_CHECK_NEEDED;
			return TypeRelation.NOT_COMPATIBLE;
		}

		@Override
		boolean dynamicCheck(Object o) {
			// Object can be either Long or Float/Double
			if(o instanceof Long) {
				long value = ((Number)o).longValue();
				return Integer.MIN_VALUE <= value && value <= Integer.MAX_VALUE; 
			} else {
				double value = ((Number)o).doubleValue();
				if(Double.isNaN(value)) return false;
				return Integer.MIN_VALUE <= value && value <= Integer.MAX_VALUE;
			}
		}

	}
	
	/**
	 * Checks for cast on i64 and v64.
	 * @author olibroe
	 *
	 */
	private class I64Check extends Check {
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			int id = oldType.typeID;
			// compatible with i8,i16,i32,i64,v64
			if(7 <= id && id <= 11) return TypeRelation.COMPATIBLE;
			// needs dynamic check for float types f32 and f64
			if(12 <= id && id <= 13) return TypeRelation.DYN_CHECK_NEEDED;
			return TypeRelation.NOT_COMPATIBLE;
		}

		@Override
		boolean dynamicCheck(Object o) {
			// check only possible for floating point numbers
			double value = ((Number)o).doubleValue();
			if(Double.isNaN(value)) return false;
			return Long.MIN_VALUE <= value && value <= Long.MAX_VALUE;
		}

	}
	
	/**
	 * Checks for cast on f32.
	 * @author olibroe
	 *
	 */
	private class F32Check extends Check {
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			int id = oldType.typeID;
			// cast from all number types possible
			if(7 <= id && id <= 13) return TypeRelation.COMPATIBLE;
			return TypeRelation.NOT_COMPATIBLE;
		}

		@Override
		boolean dynamicCheck(Object o) {
			// not needed
			return true;
		}

	}
	
	/**
	 * Checks for cast on f64.
	 * @author olibroe
	 *
	 */
	private class F64Check extends Check {
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			int id = oldType.typeID;
			// cast from all number types possible
			if(7 <= id && id <= 13) return TypeRelation.COMPATIBLE;
			return TypeRelation.NOT_COMPATIBLE;
		}

		@Override
		boolean dynamicCheck(Object o) {
			// not needed
			return true;
		}

	}
	
	/**
	 * Checks for cast on String.
	 * @author olibroe
	 *
	 */
	private class StringCheck extends Check {
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			// only string to string possible
			if(oldType.typeID == 14) return TypeRelation.COMPATIBLE;
			return TypeRelation.NOT_COMPATIBLE;
		}

		@Override
		boolean dynamicCheck(Object o) {
			// string does not need
			return true;
		}

	}
	
	/**
	 * Checks for cast on Constant Length Arrays T[i].
	 * The check needs also to check the ground type.
	 * @author olibroe
	 *
	 */
	private class ConstantLengthArrayCheck extends Check {
		private Check groundTypeCheck;
		private ConstantLengthArray<?> cla;
		
		ConstantLengthArrayCheck(FieldType<?> newFieldType) {
			groundTypeCheck = dispatchField(((SingleArgumentType<?,?>)newFieldType).groundType);
			cla = (ConstantLengthArray<?>) newFieldType;
		}
		
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			int id = oldType.typeID;
			// if the old field is also constant length array we can directly check the length and return the result
			// for the basetypes
			// all other single argument types (T[],list<T>,set<T>) need a dynamic check if ground types can
			// be compatible
			if(id == 15) {
				if(cla.length == ((ConstantLengthArray<?>)oldType).length) {
					return groundTypeCheck.staticCheck(((SingleArgumentType<?, ?>)oldType).groundType);
				}
			} else if(17 <= id && id <= 19) {
				// for all other single argument types we need a dynamic size check,
				// so we return downcast if groundTypes are not unrelated
				if(groundTypeCheck.staticCheck(((SingleArgumentType<?, ?>)oldType).groundType) 
						!= TypeRelation.NOT_COMPATIBLE) return TypeRelation.DYN_CHECK_NEEDED;
			}
			return TypeRelation.NOT_COMPATIBLE;
		}

		@Override
		boolean dynamicCheck(Object o) {
			Collection<?> coll = (Collection<?>) o;
			// check length
			if(coll.size() != cla.length) return false;
			// check objects
			for(Object obj : coll) {
				if(!groundTypeCheck.dynamicCheck(obj)) return false;
			}
			return true;
		}

	}
	
	/**
	 * Checks for cast on Variable Length Collections T[], set<T> and list<T>.
	 * The check needs also to check the ground type.
	 * @author olibroe
	 *
	 */
	private class VariableLengthCollectionCheck extends Check {
		private Check groundTypeCheck;
		
		VariableLengthCollectionCheck(FieldType<?> newFieldType) {
			groundTypeCheck = dispatchField(((SingleArgumentType<?,?>)newFieldType).groundType);
		}
		
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			int id = oldType.typeID;
			// compatible with all other single argument types if basetypes can be compatible
			if(15 <= id && id <= 19) {
				return groundTypeCheck.staticCheck(((SingleArgumentType<?, ?>)oldType).groundType);
			}
			return TypeRelation.NOT_COMPATIBLE;
		}

		@Override
		boolean dynamicCheck(Object o) {
			Collection<?> coll = (Collection<?>) o;
			// check objects in collection
			for(Object obj : coll) {
				if(!groundTypeCheck.dynamicCheck(obj)) return false;
			}
			return true;
		}

	}
	
	/**
	 * Checks for cast on map.
	 * @author olibroe
	 *
	 */
	private class MapCheck extends Check {
		private Check keyTypeCheck;
		private Check valueTypeCheck;
		
		MapCheck(FieldType<?> newFieldType) {
			keyTypeCheck = dispatchField(((MapType<?,?>)newFieldType).keyType);
			valueTypeCheck = dispatchField(((MapType<?,?>)newFieldType).valueType);
		}
		
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			// only compatible with itself
			if(oldType.typeID != 20) return TypeRelation.NOT_COMPATIBLE;
			
			// check key and value types; nested maps are checked recursively
			TypeRelation keyRel = keyTypeCheck.staticCheck(((MapType<?,?>)oldType).keyType);
			TypeRelation valueRel = valueTypeCheck.staticCheck(((MapType<?,?>)oldType).valueType);

			if(keyRel == TypeRelation.NOT_COMPATIBLE || valueRel == TypeRelation.NOT_COMPATIBLE) return TypeRelation.NOT_COMPATIBLE;
			if(keyRel == TypeRelation.DYN_CHECK_NEEDED || valueRel == TypeRelation.DYN_CHECK_NEEDED) return TypeRelation.DYN_CHECK_NEEDED;
			return TypeRelation.COMPATIBLE;
		}

		@Override
		boolean dynamicCheck(Object o) {
			Map<?,?> map = (Map<?,?>) o;
			for(Entry<?,?> entry : map.entrySet()) {
				if(!keyTypeCheck.dynamicCheck(entry.getKey())) return false;
				if(!valueTypeCheck.dynamicCheck(entry.getValue())) return false;
			}
			
			return true;
		}

	}
	
	/**
	 * Checks for cast on usertype.
	 * @author olibroe
	 *
	 */
	private class UsertypeCheck extends Check {
		private FieldType<?> newType;
		
		UsertypeCheck(FieldType<?> newFieldType) {
			newType = newFieldType;
		}
		
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			// only compatible with annotation or usertype
			if(oldType.typeID == 5) return TypeRelation.DYN_CHECK_NEEDED;
			if(oldType.typeID < 32) return TypeRelation.NOT_COMPATIBLE;
			
			StoragePool<?,?> mappedPool = specificationMapper.poolMapping.get((StoragePool<?, ?>) oldType);
			StoragePool<?,?> newPool = (StoragePool<?, ?>) newType;

			// check for equality and upcast
			while(mappedPool != null) {
				if(mappedPool.equals(newPool)) return TypeRelation.COMPATIBLE;
				mappedPool = mappedPool.superPool;
			}
			
			// there is a theoretical check if downcast is possible, but in this implementation
			// this step is skipped, because the dynamic check is sufficient here
			return TypeRelation.DYN_CHECK_NEEDED;
		}

		@Override
		boolean dynamicCheck(Object o) {
			// O(1) algorithm based on "Determining Type, Part, Color and Time relationships" 
			// by LK Schubert, MA Papalaskaris, J Taugher
			SkillObject newO = specificationMapper.calculateNewSkillObject((SkillObject) o);

			int objID = newO.getSkillID();
			int bpo = specificationMapper.newLbpoMap[newType.typeID - 32];
			return bpo < objID && objID <= bpo + ((StoragePool<?,?>)newType).size();
		}

	}

}
