package de.ust.skill.skillManipulator;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import de.ust.skill.common.java.api.SkillException;
import de.ust.skill.common.java.internal.FieldDeclaration;
import de.ust.skill.common.java.internal.FieldType;
import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.common.java.internal.StoragePool;
import de.ust.skill.common.java.internal.TypeHierarchyIterator;
import de.ust.skill.common.java.internal.fieldTypes.ConstantLengthArray;
import de.ust.skill.common.java.internal.fieldTypes.FloatType;
import de.ust.skill.common.java.internal.fieldTypes.IntegerType;
import de.ust.skill.common.java.internal.fieldTypes.MapType;
import de.ust.skill.common.java.internal.fieldTypes.SingleArgumentType;

public class FieldCompatibilityChecker {
	protected enum TypeRelation { EQUAL, UPCAST, DOWNCAST, NOT_RELATED };
	
	private static SpecificationMapper specificationMapper;
	
	public FieldCompatibilityChecker(SpecificationMapper specificationMapper) {
		FieldCompatibilityChecker.specificationMapper = specificationMapper;
	}
	
	public boolean fieldsCompatible(FieldDeclaration<?, ?> oldField, FieldDeclaration<?, ?> newField) {
		Check check = dispatchField(newField.type());
		
		if(check == null) throw new SkillException("New Type is unknown");
		
		TypeRelation tr = check.staticCheck(oldField.type());
		
		if(oldField.type() instanceof FloatType<?> && newField.type() instanceof IntegerType) {
			MappingLog.genIntFloatWarning(oldField, newField);
		}
		
		if(oldField.type() instanceof IntegerType && newField.type() instanceof FloatType<?>) {
			MappingLog.genIntFloatWarning(oldField, newField);
		}
		
		if(tr == TypeRelation.NOT_RELATED) return false;

		if(tr == TypeRelation.DOWNCAST) {
			return oldField.owner().stream()
					.map(obj -> oldField.get(obj))
					.allMatch(o -> check.dynamicCheck(o)); 
		}
		
		return  true;
	}

	private static Check dispatchField(FieldType<?> newType) {
		int typeID = newType.typeID;
		
		if(typeID >= 32) return new UsertypeCheck(newType);
		switch(typeID) {
		case 5: return AnnotationCheck.get();
		case 6: return BoolCheck.get();
		case 7: return ByteCheck.get();
		case 8: return ShortCheck.get();
		case 9: return IntCheck.get();
		case 10:
		case 11:
			return LongCheck.get();
		case 12: return FloatCheck.get();
		case 13: return DoubleCheck.get();
		case 14: return StringCheck.get();
		case 15: return new ConstantLengthArrayCheck(newType);
		case 17:
		case 18:
		case 19:
			return new VariableLengthCollectionCheck(newType);
		case 20:
			return new MapCheck(newType);
		default:
			return null;
		}
	}
	
	private static abstract class Check {	
		abstract TypeRelation staticCheck(FieldType<?> oldType);
		abstract boolean dynamicCheck(Object o);
	}
	
	private static class AnnotationCheck extends Check {
		private static final AnnotationCheck check = new AnnotationCheck();
		
		static Check get() {
			return check;
		}
		
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			if(oldType.typeID == 5) return TypeRelation.EQUAL;
			return TypeRelation.NOT_RELATED;
		}

		@Override
		boolean dynamicCheck(Object o) {
			// not needed for annotation
			return true;
		}

	}
	
	private static class BoolCheck extends Check {
		private static final BoolCheck check = new BoolCheck();
		
		static Check get() {
			return check;
		}
		
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			if(oldType.typeID == 6) return TypeRelation.EQUAL;
			return TypeRelation.NOT_RELATED;
		}

		@Override
		boolean dynamicCheck(Object o) {
			// not needed for bool
			return true;
		}

	}
	
	private static class ByteCheck extends Check {
		private static final ByteCheck check = new ByteCheck();
		
		static Check get() {
			return check;
		}
		
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			int id = oldType.typeID;
			if(id == 7) return TypeRelation.EQUAL;
			if(8 <= id && id <= 13) {
				return TypeRelation.DOWNCAST;
			}
			return TypeRelation.NOT_RELATED;
		}

		@Override
		boolean dynamicCheck(Object o) {
			byte value = ((Number)o).byteValue();
			return Byte.MIN_VALUE <= value && value <= Byte.MAX_VALUE; 
		}

	}
	
	private static class ShortCheck extends Check {
		private static final ShortCheck check = new ShortCheck();
		
		static Check get() {
			return check;
		}
		
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			int id = oldType.typeID;
			if(id == 8) return TypeRelation.EQUAL;
			if(9 <= id && id <= 13) {
				return TypeRelation.DOWNCAST;
			}
			if(id == 7) return TypeRelation.UPCAST;
			return TypeRelation.NOT_RELATED;
		}

		@Override
		boolean dynamicCheck(Object o) {
			short value = ((Number)o).shortValue();
			return Short.MIN_VALUE <= value && value <= Short.MAX_VALUE; 
		}

	}
	
	private static class IntCheck extends Check {
		private static final IntCheck check = new IntCheck();
		
		static Check get() {
			return check;
		}
		
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			int id = oldType.typeID;
			if(id == 9) return TypeRelation.EQUAL;
			if(10 <= id && id <= 13) {
				return TypeRelation.DOWNCAST;
			}
			if(7 <= id && id <= 8) return TypeRelation.UPCAST;
			return TypeRelation.NOT_RELATED;
		}

		@Override
		boolean dynamicCheck(Object o) {
			int value = ((Number)o).intValue();
			return Integer.MIN_VALUE <= value && value <= Integer.MAX_VALUE; 
		}

	}
	
	private static class LongCheck extends Check {
		private static final LongCheck check = new LongCheck();
		
		static Check get() {
			return check;
		}
		
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			int id = oldType.typeID;
			if(id == 10 || id == 11) return TypeRelation.EQUAL;
			if(12 <= id && id <= 13) {
				return TypeRelation.DOWNCAST;
			}
			if(7 <= id && id <= 9) return TypeRelation.UPCAST;
			return TypeRelation.NOT_RELATED;
		}

		@Override
		boolean dynamicCheck(Object o) {
			// check not needed, downcast can only happen from float and double
			return true;
		}

	}
	
	private static class FloatCheck extends Check {
		private static final FloatCheck check = new FloatCheck();
		
		static Check get() {
			return check;
		}
		
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			int id = oldType.typeID;
			if(id == 12) return TypeRelation.EQUAL;
			if(id == 13) return TypeRelation.DOWNCAST;
			if(7 <= id && id <= 11) return TypeRelation.UPCAST;
			return TypeRelation.NOT_RELATED;
		}

		@Override
		boolean dynamicCheck(Object o) {
			// TODO Timm fragen
			return true;
		}

	}
	
	private static class DoubleCheck extends Check {
		private static final DoubleCheck check = new DoubleCheck();
		
		static Check get() {
			return check;
		}
		
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			int id = oldType.typeID;
			if(id == 13) return TypeRelation.EQUAL;
			if(7 <= id && id <= 12) return TypeRelation.UPCAST;
			return TypeRelation.NOT_RELATED;
		}

		@Override
		boolean dynamicCheck(Object o) {
			// double is biggest data type
			return true;
		}

	}
	
	private static class StringCheck extends Check {
		private static final StringCheck check = new StringCheck();
		
		static Check get() {
			return check;
		}
		
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			if(oldType.typeID == 14) return TypeRelation.EQUAL;
			return TypeRelation.NOT_RELATED;
		}

		@Override
		boolean dynamicCheck(Object o) {
			// string does not need
			return true;
		}

	}
	
	private static class ConstantLengthArrayCheck extends Check {
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
				if(cla.length == ((ConstantLengthArray<?>)oldType).length) return TypeRelation.EQUAL;
			} else if(17 <= id && id <= 19) {
				// for all other single argument types we need a dynamic size check, so we return downcast if groundTypes are not unrelated
				if(groundTypeCheck.staticCheck(((SingleArgumentType<?, ?>)oldType).groundType) != TypeRelation.NOT_RELATED) return TypeRelation.DOWNCAST;
			}
			return TypeRelation.NOT_RELATED;
		}

		@Override
		boolean dynamicCheck(Object o) {
			Collection<?> coll = (Collection<?>) o;
			if(coll.size() != cla.length) return false;
			return coll.stream().allMatch(obj -> groundTypeCheck.dynamicCheck(obj));
		}

	}
	
	private static class VariableLengthCollectionCheck extends Check {
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
			return TypeRelation.NOT_RELATED;
		}

		@Override
		boolean dynamicCheck(Object o) {
			Collection<?> coll = (Collection<?>) o;
			return coll.stream().allMatch(obj -> groundTypeCheck.dynamicCheck(obj));
		}

	}
	
	private static class MapCheck extends Check {
		private Check keyTypeCheck;
		private Check valueTypeCheck;
		
		MapCheck(FieldType<?> newFieldType) {
			keyTypeCheck = dispatchField(((MapType<?,?>)newFieldType).keyType);
			valueTypeCheck = dispatchField(((MapType<?,?>)newFieldType).valueType);
		}
		
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			if(oldType.typeID != 20) return TypeRelation.NOT_RELATED;
			
			TypeRelation keyRel = keyTypeCheck.staticCheck(((MapType<?,?>)oldType).keyType);
			TypeRelation valueRel = valueTypeCheck.staticCheck(((MapType<?,?>)oldType).valueType);

			if(keyRel == TypeRelation.NOT_RELATED || valueRel == TypeRelation.NOT_RELATED) return TypeRelation.NOT_RELATED;
			if(keyRel == TypeRelation.DOWNCAST || valueRel == TypeRelation.DOWNCAST) return TypeRelation.DOWNCAST;
			if(keyRel == TypeRelation.UPCAST || valueRel == TypeRelation.UPCAST) return TypeRelation.UPCAST;
			return TypeRelation.EQUAL;
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
	
	private static class UsertypeCheck extends Check {
		private FieldType<?> newType;
		
		UsertypeCheck(FieldType<?> newFieldType) {
			newType = newFieldType;
		}
		
		@Override
		TypeRelation staticCheck(FieldType<?> oldType) {
			if(oldType.typeID == 5) return TypeRelation.DOWNCAST;
			if(oldType.typeID < 32) return TypeRelation.NOT_RELATED;
			
			StoragePool<?,?> mappedPool = specificationMapper.poolMapping.get((StoragePool<?, ?>) oldType);
			StoragePool<?,?> newPool = (StoragePool<?, ?>) newType;

			if(mappedPool != null) {
				if(mappedPool.equals(newPool)) return TypeRelation.EQUAL;
				
				// check for upcast
				while(mappedPool.superPool != null) {
					mappedPool = mappedPool.superPool;
					if(mappedPool.equals(newPool)) return TypeRelation.UPCAST;
				}
			}
			
			// check for downcast, start on old side and look at mapped pool on new side
		    // from mapped pools on new side look at superpools,
			// this is because of additional pools that are created in between
			TypeHierarchyIterator<?, ?> hierarchyIt = new TypeHierarchyIterator<>((StoragePool<?, ?>) oldType);
			StoragePool<?,?> pool;
			StoragePool<?,?> superPool;
			while(hierarchyIt.hasNext()) {
				pool = hierarchyIt.next();
				mappedPool = specificationMapper.poolMapping.get(pool);
				if(mappedPool != null) {
					if(mappedPool.equals(newPool)) return TypeRelation.DOWNCAST;
					superPool = mappedPool.superPool;
					while(superPool != null) {
						if(superPool.equals(newPool)) return TypeRelation.DOWNCAST;
						superPool = superPool.superPool;
					}
				}
			}
			
			return TypeRelation.NOT_RELATED;
		}

		@Override
		boolean dynamicCheck(Object o) {
			SkillObject newO = specificationMapper.calculateNewSkillObject((SkillObject) o);
			if(newO != null) {
				StoragePool<?,?> objectPool = specificationMapper.newState.pool(newO.skillName());
				while(objectPool != null) {
					if(objectPool.equals(newType)) return true;
					objectPool = objectPool.superPool;
				}
				return false;
			}
			return true;
		}

	}

}
