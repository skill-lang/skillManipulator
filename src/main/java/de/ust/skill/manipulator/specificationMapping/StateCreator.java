package de.ust.skill.manipulator.specificationMapping;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.ust.skill.common.java.api.SkillFile.ActualMode;
import de.ust.skill.common.java.api.SkillFile.Mode;
import de.ust.skill.common.java.internal.BasePool;
import de.ust.skill.common.java.internal.FieldDeclaration;
import de.ust.skill.common.java.internal.FieldType;
import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.common.java.internal.StoragePool;
import de.ust.skill.common.java.internal.StringPool;
import de.ust.skill.common.java.internal.fieldTypes.Annotation;
import de.ust.skill.common.java.internal.fieldTypes.BoolType;
import de.ust.skill.common.java.internal.fieldTypes.ConstantI16;
import de.ust.skill.common.java.internal.fieldTypes.ConstantI32;
import de.ust.skill.common.java.internal.fieldTypes.ConstantI64;
import de.ust.skill.common.java.internal.fieldTypes.ConstantI8;
import de.ust.skill.common.java.internal.fieldTypes.ConstantLengthArray;
import de.ust.skill.common.java.internal.fieldTypes.ConstantV64;
import de.ust.skill.common.java.internal.fieldTypes.F32;
import de.ust.skill.common.java.internal.fieldTypes.F64;
import de.ust.skill.common.java.internal.fieldTypes.I16;
import de.ust.skill.common.java.internal.fieldTypes.I32;
import de.ust.skill.common.java.internal.fieldTypes.I64;
import de.ust.skill.common.java.internal.fieldTypes.I8;
import de.ust.skill.common.java.internal.fieldTypes.ListType;
import de.ust.skill.common.java.internal.fieldTypes.MapType;
import de.ust.skill.common.java.internal.fieldTypes.SetType;
import de.ust.skill.common.java.internal.fieldTypes.V64;
import de.ust.skill.common.java.internal.fieldTypes.VariableLengthArray;
import de.ust.skill.common.java.restrictions.Abstract;
import de.ust.skill.common.java.restrictions.Coding;
import de.ust.skill.common.java.restrictions.ConstantLengthPointer;
import de.ust.skill.common.java.restrictions.DefaultValue;
import de.ust.skill.common.java.restrictions.FieldRestriction;
import de.ust.skill.common.java.restrictions.Monotone;
import de.ust.skill.common.java.restrictions.NonNull;
import de.ust.skill.common.java.restrictions.Range;
import de.ust.skill.common.java.restrictions.Singleton;
import de.ust.skill.common.java.restrictions.TypeRestriction;
import de.ust.skill.common.java.restrictions.Unique;
import de.ust.skill.common.jvm.streams.FileInputStream;
import de.ust.skill.ir.ConstantLengthArrayType;
import de.ust.skill.ir.Field;
import de.ust.skill.ir.GroundType;
import de.ust.skill.ir.Restriction;
import de.ust.skill.ir.Type;
import de.ust.skill.ir.TypeContext;
import de.ust.skill.ir.UserType;
import de.ust.skill.ir.VariableLengthArrayType;
import de.ust.skill.ir.restriction.AbstractRestriction;
import de.ust.skill.ir.restriction.CodingRestriction;
import de.ust.skill.ir.restriction.ConstantLengthPointerRestriction;
import de.ust.skill.ir.restriction.FloatDefaultRestriction;
import de.ust.skill.ir.restriction.FloatRangeRestriction;
import de.ust.skill.ir.restriction.IntDefaultRestriction;
import de.ust.skill.ir.restriction.IntRangeRestriction;
import de.ust.skill.ir.restriction.MonotoneRestriction;
import de.ust.skill.ir.restriction.NameDefaultRestriction;
import de.ust.skill.ir.restriction.NonNullRestriction;
import de.ust.skill.ir.restriction.SingletonRestriction;
import de.ust.skill.ir.restriction.StringDefaultRestriction;
import de.ust.skill.ir.restriction.UniqueRestriction;
import de.ust.skill.manipulator.internal.SkillState;

public class StateCreator {

	private Annotation annotation;
	private SkillState newState;
	private StringPool strings;
	private HashMap<String,StoragePool<?,?>> poolByName;

	protected static SkillState createNewState(TypeContext tc, Path targetPath) throws IOException {
		return new StateCreator(tc, targetPath).newState;
	}
	
	@SuppressWarnings("unchecked")
	private <B extends SkillObject, T extends B> StateCreator(TypeContext tc, Path targetPath) throws IOException {
		// create state arguments
		ActualMode actualMode = new ActualMode(Mode.Create, Mode.Write);
		strings = new StringPool(null);
		ArrayList<StoragePool<?, ?>> types = new ArrayList<>(tc.getUsertypes().size());
		poolByName = new HashMap<>();
		annotation = new Annotation(types);

		
		StoragePool<T, B> newPool = null;
		StoragePool<? super T, B> superPool;
		UserType superType;
		
		// create new Pool for every UserType
		for(UserType utype : tc.getUsertypes()) {
			// check if there is a super pool
			superType = utype.getSuperType();
			superPool = (StoragePool<? super T, B>) (superType == null ? null : poolByName.get(superType.getSkillName()));

			// if no super pool => the new pool is a basepool
			// else the pool is a subpool of its super pool
			if (null == superPool)
				newPool = (StoragePool<T, B>) new BasePool<T>(types.size(), utype.getSkillName(), StoragePool.noKnownFields, StoragePool.noAutoFields());
			else
				newPool = (StoragePool<T, B>) superPool.makeSubPool(types.size(), utype.getSkillName());

			TypeRestriction tr;
			for(Restriction r : utype.getRestrictions()) {
				tr = createTypeRestriction(r);
				if(tr != null) {
					newPool.addRestriction(tr);
					if(tr instanceof DefaultValue) {
						newPool.defaultValue = (DefaultValue<?>) tr;
					}
				}
			}
			
			// put new pool in map and list
			poolByName.put(utype.getSkillName(), newPool);
			types.add(newPool);
		}

		
		// the state has to be created at this point, because the pool function is needed for creation of fields
		newState = new SkillState(poolByName, strings, annotation, 
				types, FileInputStream.open(targetPath, false), actualMode.close);

		
		FieldType<?> fType;
		StoragePool<?,?> currentPool;
		FieldRestriction<?> fr;
		FieldDeclaration<?, ?> newField;
		
		// create fields of types
		// have to run over UserTypes a second time because types have to be known to create fields
		for(UserType utype : tc.getUsertypes()) {
			currentPool = poolByName.get(utype.getSkillName());
			for(Field f : utype.getFields()) {
				if(!f.isAuto()) {
					// get right field type
					if(f.isConstant()) fType = getConstantType(f.getType(), f.constantValue());
					else fType = getFieldType(f.getType());
					
					// add field to pool
					// note: fType should never be null here
					if(fType != null) {
						newField = currentPool.addField(fType, f.getSkillName());
						
						for(Restriction r : f.getRestrictions()) {
							fr = createFieldRestriction(r, newField);
							if(fr != null) {
								newField.addRestriction(fr);
								if(fr instanceof DefaultValue) {
									newField.defaultValue = (DefaultValue<?>) fr;
								}
							}
						}
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private FieldRestriction<?> createFieldRestriction(Restriction r, FieldDeclaration<?,?> newField) {
		if(r instanceof IntRangeRestriction) {
			return Range.make(newField.type().typeID, ((IntRangeRestriction)r).getLow(), ((IntRangeRestriction)r).getHigh());
		}
		if(r instanceof FloatRangeRestriction) {
			return Range.make(newField.type().typeID, ((FloatRangeRestriction)r).getLowDouble(), ((FloatRangeRestriction)r).getHighDouble());
		}
		if(r instanceof NonNullRestriction) {
			return NonNull.get();
		}
		if(r instanceof FloatDefaultRestriction) {
			switch(newField.type().typeID) {
			case 12:
				return DefaultValue.makeFieldRestriction((float)((FloatDefaultRestriction)r).getValue(), (FieldType<Float>) newField.type());
			case 13:
				return DefaultValue.makeFieldRestriction(((FloatDefaultRestriction)r).getValue(), (FieldType<Double>) newField.type());
			}
		}
		if(r instanceof IntDefaultRestriction) {
			switch(newField.type().typeID) {
			case 7:
				return DefaultValue.makeFieldRestriction((byte)((IntDefaultRestriction)r).getValue(), (FieldType<Byte>) newField.type());
			case 8:
				return DefaultValue.makeFieldRestriction((short)((IntDefaultRestriction)r).getValue(), (FieldType<Short>) newField.type());
			case 9:
				return DefaultValue.makeFieldRestriction((int)((IntDefaultRestriction)r).getValue(), (FieldType<Integer>) newField.type());
			case 10:
			case 11:
				return DefaultValue.makeFieldRestriction(((IntDefaultRestriction)r).getValue(), (FieldType<Long>) newField.type());
			}
		}
		if(r instanceof StringDefaultRestriction) {
			return DefaultValue.makeFieldRestriction(((StringDefaultRestriction)r).getValue(), (FieldType<String>) newField.type());
		}
		if(r instanceof NameDefaultRestriction) {
			if(((NameDefaultRestriction)r).getValue().isEmpty()) return null;
			return DefaultValue.makeFieldRestriction(((NameDefaultRestriction)r).getValue().get(0).getSkillName(), poolByName);
		}
		if(r instanceof CodingRestriction) {
			return new Coding<>(((CodingRestriction)r).getValue(), strings);
		}
		if(r instanceof ConstantLengthPointerRestriction) {
			return ConstantLengthPointer.get();
		}
		return null;
	}

	private TypeRestriction createTypeRestriction(Restriction r) {
		if(r instanceof SingletonRestriction) {
			return Singleton.get();
		}
		if(r instanceof UniqueRestriction) {
			return Unique.get();
		}
		if(r instanceof MonotoneRestriction) {
			return Monotone.get();
		}
		if(r instanceof AbstractRestriction) {
			return Abstract.get();
		}
		if(r instanceof NameDefaultRestriction) {
			if(((NameDefaultRestriction)r).getValue().isEmpty()) return null;
			return DefaultValue.makeTypeRestriction(((NameDefaultRestriction)r).getValue().get(0).getSkillName(), poolByName);
		}
		return null;		
	}

	private FieldType<?> getConstantType(Type type, long value) {
		switch (type.getSkillName()) {
		case "i8":
			return new ConstantI8((byte) value);
		case "i16":
			return new ConstantI16((short) value);
		case "i32":
			return new ConstantI32((int) value);
		case "i64":
			return new ConstantI64(value);
		case "v64":
			return new ConstantV64(value);
		default:
			return null;
		}
		
	}

	private FieldType<?> getFieldType(Type type) {
		if(type instanceof GroundType) {
			switch (type.getSkillName()) {
			case "i8":
				return I8.get();
			case "i16":
				return I16.get();
			case "i32":
				return I32.get();
			case "i64":
				return I64.get();
			case "v64":
				return V64.get();
			case "annotation":
				return annotation;
			case "bool":
				return BoolType.get();
			case "f32":
				return F32.get();
			case "f64":
				return F64.get();
			case "string":
				return strings;
			}
		} else if(type instanceof ConstantLengthArrayType) {
			return new ConstantLengthArray<>(((ConstantLengthArrayType)type).getLength(), getFieldType(((ConstantLengthArrayType)type).getBaseType()));
		} else if(type instanceof VariableLengthArrayType) {
			return new VariableLengthArray<>(getFieldType(((VariableLengthArrayType)type).getBaseType()));
		} else if(type instanceof de.ust.skill.ir.ListType) {
			return new ListType<>(getFieldType(((de.ust.skill.ir.ListType)type).getBaseType()));
		} else if(type instanceof de.ust.skill.ir.SetType) {
			return new SetType<>(getFieldType(((de.ust.skill.ir.SetType)type).getBaseType()));
		} else if(type instanceof de.ust.skill.ir.MapType) {
			List<Type> list = new ArrayList<>(((de.ust.skill.ir.MapType)type).getBaseTypes());
			// combine last two base types to map
			MapType<?,?> map = new MapType<>(getFieldType(list.remove(list.size() - 2)), getFieldType(list.remove(list.size() - 1)));
			while(list.size() > 0) {
				map = new MapType<>(getFieldType(list.remove(list.size() - 1)), map);
			}
			return map;
		} else if(type instanceof UserType) {
			return newState.pool(type.getSkillName());
		}
		return null;
	}
}
