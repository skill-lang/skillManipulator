package specificationMapping;

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
	
	public boolean fieldsCompatible(FieldDeclaration<?, ?> oldField, FieldDeclaration<?, ?> newField) {
		Check check = dispatchField(newField.type());
		
		if(check == null) throw new SkillException("New Type is unknown");
		
		TypeRelation tr = check.staticCheck(oldField.type());
		
		if(tr == TypeRelation.NOT_COMPATIBLE) return false;

		if(tr == TypeRelation.DYN_CHECK_NEEDED) {
			for(SkillObject o : oldField.owner()) {
				if(!check.dynamicCheck(oldField.get(o))) return false;
			}
		}
		
		if(oldField.type() instanceof FloatType<?> && newField.type() instanceof IntegerType) {
			MappingLog.genIntFloatWarning(oldField, newField);
		}

		if(oldField.type() instanceof IntegerType && newField.type() instanceof FloatType<?>) {
			MappingLog.genIntFloatWarning(oldField, newField);
		}
		
		return  true;
	}

	private Check dispatchField(FieldType<?> newType) {
		int typeID = newType.typeID;
		
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
			return new UsertypeCheck(newType);
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
			if(oldType.typeID == 5) return TypeRelation.COMPATIBLE;
			return TypeRelation.NOT_COMPATIBLE;
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
			if(oldType.typeID == 6) return TypeRelation.COMPATIBLE;
			return TypeRelation.NOT_COMPATIBLE;
		}

		@Override
		boolean dynamicCheck(Object o) {
			// can not happen for bool
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
	
	private static class ShortCheck extends Check {
		private static final ShortCheck check = new ShortCheck();
		
		static Check get() {
			return check;
		}
		
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
	
	private static class IntCheck extends Check {
		private static final IntCheck check = new IntCheck();
		
		static Check get() {
			return check;
		}
		
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
	
	private static class LongCheck extends Check {
		private static final LongCheck check = new LongCheck();
		
		static Check get() {
			return check;
		}
		
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
	
	private static class FloatCheck extends Check {
		private static final FloatCheck check = new FloatCheck();
		
		static Check get() {
			return check;
		}
		
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
	
	private static class DoubleCheck extends Check {
		private static final DoubleCheck check = new DoubleCheck();
		
		static Check get() {
			return check;
		}
		
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
	
	private static class StringCheck extends Check {
		private static final StringCheck check = new StringCheck();
		
		static Check get() {
			return check;
		}
		
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
				if(cla.length == ((ConstantLengthArray<?>)oldType).length) return TypeRelation.COMPATIBLE;
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
