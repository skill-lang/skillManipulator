package de.ust.skill.skillManipulator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import de.ust.skill.common.java.api.SkillFile.ActualMode;
import de.ust.skill.common.java.api.SkillFile.Mode;
import de.ust.skill.common.java.internal.BasePool;
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
import de.ust.skill.common.java.internal.fieldTypes.StringType;
import de.ust.skill.common.java.internal.fieldTypes.V64;
import de.ust.skill.common.java.internal.fieldTypes.VariableLengthArray;
import de.ust.skill.common.jvm.streams.FileInputStream;
import de.ust.skill.ir.ConstantLengthArrayType;
import de.ust.skill.ir.Field;
import de.ust.skill.ir.GroundType;
import de.ust.skill.ir.Type;
import de.ust.skill.ir.TypeContext;
import de.ust.skill.ir.UserType;
import de.ust.skill.ir.VariableLengthArrayType;

public class StateCreator {

	private StringType stringType;
	private Annotation annotation;
	private SkillState newState;

	protected static SkillState createNewState(TypeContext tc, Path targetPath) throws IOException {
		return new StateCreator(tc, targetPath).newState;
	}
	
	@SuppressWarnings("unchecked")
	private <B extends SkillObject, T extends B> StateCreator(TypeContext tc, Path targetPath) throws IOException {
		// create state arguments
		ActualMode actualMode = new ActualMode(Mode.Create, Mode.Write);
		StringPool strings = new StringPool(null);
		ArrayList<StoragePool<?, ?>> types = new ArrayList<>(tc.getUsertypes().size());
		HashMap<String,StoragePool<?,?>> poolByName = new HashMap<>();
		stringType = new StringType(strings);
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

			// put new pool in map and list
			poolByName.put(utype.getSkillName(), newPool);
			types.add(newPool);
		}

		
		// the state has to be created at this point, because the pool function is needed for creation of fields
		newState = new SkillState(poolByName, strings, stringType, annotation, 
				types, FileInputStream.open(targetPath, false), actualMode.close);

		
		FieldType<?> fType;
		StoragePool<?,?> currentPool;
		
		// create fields of types
		// have to run over UserTypes a second time because types have to be known to create fields
		for(UserType utype : tc.getUsertypes()) {
			currentPool = poolByName.get(utype.getSkillName());
			for(Field f : utype.getFields()) {
				// get right field type
				if(f.isConstant()) fType = getConstantType(f.getType(), f.constantValue());
				else fType = getFieldType(f.getType());
				
				// add field to pool
				// note: fType should never be null here
				if(fType != null) currentPool.addField(fType, f.getSkillName());
			}
		}
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
				return newState.annotationType;
			case "bool":
				return BoolType.get();
			case "f32":
				return F32.get();
			case "f64":
				return F64.get();
			case "string":
				return newState.stringType;
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
