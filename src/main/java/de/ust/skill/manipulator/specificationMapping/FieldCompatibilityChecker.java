package de.ust.skill.manipulator.specificationMapping;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import de.ust.skill.common.java.api.SkillException;
import de.ust.skill.common.java.internal.FieldDeclaration;
import de.ust.skill.common.java.internal.FieldType;
import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.common.java.internal.StoragePool;
import de.ust.skill.common.java.internal.fieldTypes.ConstantLengthArray;
import de.ust.skill.common.java.internal.fieldTypes.FloatType;
import de.ust.skill.common.java.internal.fieldTypes.IntegerType;
import de.ust.skill.common.java.internal.fieldTypes.MapType;
import de.ust.skill.common.java.internal.fieldTypes.SingleArgumentType;

public class FieldCompatibilityChecker {
	protected enum TypeRelation { COMPATIBLE, DYN_CHECK_NEEDED, NOT_COMPATIBLE };
	
	private SpecificationMapper specificationMapper;
	
	public FieldCompatibilityChecker(SpecificationMapper specificationMapper) {
		this.specificationMapper = specificationMapper;
	}
	
	public boolean fieldsCompatible(FieldDeclaration<?, ?> oldField, FieldDeclaration<?, ?> newField, StoragePool<?,?> oldPool) {
		Check check = dispatchField(newField.type());
		
		TypeRelation tr = check.staticCheck(oldField.type());
		
		if(tr == TypeRelation.NOT_COMPATIBLE) return false;

		if(tr == TypeRelation.DYN_CHECK_NEEDED) {
			for(SkillObject o : oldPool) {
				if(!check.dynamicCheck(oldField.get(o))) return false;
			}
		}
		
		if(oldField.type() instanceof FloatType<?> && newField.type() instanceof IntegerType) {
			// TODO
//			MappingLog.genIntFloatWarning(oldField, newField);
		}

		if(oldField.type() instanceof IntegerType && newField.type() instanceof FloatType<?>) {
			// TODO
//			MappingLog.genIntFloatWarning(oldField, newField);
		}
		
		return  true;
	}

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
	
	private final AnnotationCheck annotationCheck = new AnnotationCheck();
	private final BoolCheck boolCheck = new BoolCheck();
	private final ByteCheck byteCheck = new ByteCheck();
	private final ShortCheck shortCheck = new ShortCheck();
	private final IntCheck intCheck = new IntCheck();
	private final LongCheck longCheck = new LongCheck();
	private final FloatCheck floatCheck = new FloatCheck();
	private final DoubleCheck doubleCheck = new DoubleCheck();
	private final StringCheck stringCheck = new StringCheck();
	
	private abstract class Check {	
		abstract TypeRelation staticCheck(FieldType<?> oldType);
		abstract boolean dynamicCheck(Object o);
	}
	
	private class AnnotationCheck extends Check {
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			if(oldType.typeID == 5 || oldType.typeID >= 32) return TypeRelation.COMPATIBLE;
			return TypeRelation.NOT_COMPATIBLE;
		}

		@Override
		boolean dynamicCheck(Object o) {
			// not needed for annotation
			return true;
		}

	}
	
	private class BoolCheck extends Check {
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			if(oldType.typeID == 6) return TypeRelation.COMPATIBLE;
			return TypeRelation.NOT_COMPATIBLE;
		}

		@Override
		boolean dynamicCheck(Object o) {
			// can not happen for bool
			return true;
		}

	}
	
	private class ByteCheck extends Check {
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			int id = oldType.typeID;
			if(id == 7) return TypeRelation.COMPATIBLE;
			if(8 <= id && id <= 13) return TypeRelation.DYN_CHECK_NEEDED;
			return TypeRelation.NOT_COMPATIBLE;
		}

		@Override
		boolean dynamicCheck(Object o) {
			// Object o can be either Long, Int, Short or Float/Double
			// int and short can be converted losless to double
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
	
	private class ShortCheck extends Check {
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			int id = oldType.typeID;
			if(7 <= id && id <= 8) return TypeRelation.COMPATIBLE;
			if(9 <= id && id <= 13) return TypeRelation.DYN_CHECK_NEEDED;
			return TypeRelation.NOT_COMPATIBLE;
		}

		@Override
		boolean dynamicCheck(Object o) {
			// Object o can be either Long, Int or Float/Double
			// int can be converted losless to double
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
	
	private class IntCheck extends Check {
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			int id = oldType.typeID;
			if(7 <= id && id <= 9) return TypeRelation.COMPATIBLE;
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
	
	private class LongCheck extends Check {
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			int id = oldType.typeID;
			if(7 <= id && id <= 11) return TypeRelation.COMPATIBLE;
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
	
	private class FloatCheck extends Check {
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			int id = oldType.typeID;
			if(7 <= id && id <= 13) return TypeRelation.COMPATIBLE;
			return TypeRelation.NOT_COMPATIBLE;
		}

		@Override
		boolean dynamicCheck(Object o) {
			// not needed
			return true;
		}

	}
	
	private class DoubleCheck extends Check {
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			int id = oldType.typeID;
			if(7 <= id && id <= 13) return TypeRelation.COMPATIBLE;
			return TypeRelation.NOT_COMPATIBLE;
		}

		@Override
		boolean dynamicCheck(Object o) {
			// double is biggest data type
			return true;
		}

	}
	
	private class StringCheck extends Check {
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			if(oldType.typeID == 14) return TypeRelation.COMPATIBLE;
			return TypeRelation.NOT_COMPATIBLE;
		}

		@Override
		boolean dynamicCheck(Object o) {
			// string does not need
			return true;
		}

	}
	
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
			if(id == 15) {
				if(cla.length == ((ConstantLengthArray<?>)oldType).length) {
					return groundTypeCheck.staticCheck(((SingleArgumentType<?, ?>)oldType).groundType);
				}
			} else if(17 <= id && id <= 19) {
				// for all other single argument types we need a dynamic size check, so we return downcast if groundTypes are not unrelated
				if(groundTypeCheck.staticCheck(((SingleArgumentType<?, ?>)oldType).groundType) != TypeRelation.NOT_COMPATIBLE) return TypeRelation.DYN_CHECK_NEEDED;
			}
			return TypeRelation.NOT_COMPATIBLE;
		}

		@Override
		boolean dynamicCheck(Object o) {
			Collection<?> coll = (Collection<?>) o;
			if(coll.size() != cla.length) return false;
			for(Object obj : coll) {
				if(!groundTypeCheck.dynamicCheck(obj)) return false;
			}
			return true;
		}

	}
	
	private class VariableLengthCollectionCheck extends Check {
		private Check groundTypeCheck;
		
		VariableLengthCollectionCheck(FieldType<?> newFieldType) {
			groundTypeCheck = dispatchField(((SingleArgumentType<?,?>)newFieldType).groundType);
		}
		
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			int id = oldType.typeID;
			if(15 <= id && id <= 19) {
				return groundTypeCheck.staticCheck(((SingleArgumentType<?, ?>)oldType).groundType);
			}
			return TypeRelation.NOT_COMPATIBLE;
		}

		@Override
		boolean dynamicCheck(Object o) {
			Collection<?> coll = (Collection<?>) o;
			for(Object obj : coll) {
				if(!groundTypeCheck.dynamicCheck(obj)) return false;
			}
			return true;
		}

	}
	
	private class MapCheck extends Check {
		private Check keyTypeCheck;
		private Check valueTypeCheck;
		
		MapCheck(FieldType<?> newFieldType) {
			keyTypeCheck = dispatchField(((MapType<?,?>)newFieldType).keyType);
			valueTypeCheck = dispatchField(((MapType<?,?>)newFieldType).valueType);
		}
		
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			if(oldType.typeID != 20) return TypeRelation.NOT_COMPATIBLE;
			
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
	
	private class UsertypeCheck extends Check {
		private FieldType<?> newType;
		
		UsertypeCheck(FieldType<?> newFieldType) {
			newType = newFieldType;
		}
		
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
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
			if(newO != null) {
				int objID = newO.getSkillID();
				int bpo = specificationMapper.newLbpoMap[newType.typeID - 32];
				return bpo < objID && objID <= bpo + ((StoragePool<?,?>)newType).size();
			}
			return true;
		}

	}

}
