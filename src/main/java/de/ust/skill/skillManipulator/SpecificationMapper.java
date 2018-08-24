package de.ust.skill.skillManipulator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

import de.ust.skill.common.java.internal.BasePool;
import de.ust.skill.common.java.internal.FieldDeclaration;
import de.ust.skill.common.java.internal.FieldIterator;
import de.ust.skill.common.java.internal.FieldType;
import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.common.java.internal.StaticFieldIterator;
import de.ust.skill.common.java.internal.StoragePool;
import de.ust.skill.common.java.internal.fieldTypes.MapType;
import de.ust.skill.common.java.internal.fieldTypes.SingleArgumentType;
import de.ust.skill.common.java.internal.parts.Block;
import de.ust.skill.ir.TypeContext;

public class SpecificationMapper {
	
	// maps pools from old state to new state
	protected Map<StoragePool<?,?>, StoragePool<?,?>> poolMapping = new HashMap<>();
	
	protected SkillState newState;
	private SkillState oldState;
	
	// lbpo maps of the old and new types
	private int[] newLbpoMap;
	private int[] oldLbpoMap;
	
	// the projection offset is needed if a type of the old state is projected
	// to another type in the new state, then the old type is mapped to a value that represents
	// the relative position of the projected type in the type it is projected to 
	private int[] projectionOffsetMap;
	
	private SpecificationMapper() {}
	
	public static SkillFile map(TypeContext tc, SkillFile sf, Path targetPath) {
		SpecificationMapper mapper = new SpecificationMapper();
		MappingLog.clearLog();
		
		mapper.oldState = (SkillState)sf;
		
		try {
			mapper.newState = StateCreator.createNewState(tc, targetPath);
		} catch (IOException e) {
			// TODO can not create state, abort
			e.printStackTrace();
		}
		
		mapper.mapStates();
		
		try {
			mapper.transferData();
		} catch (InterruptedException e) {
			// TODO error while allocating objects
			e.printStackTrace();
		}
		
		// TODO remove print
		mapper.newState.prettyPrint();
		
		System.out.println(MappingLog.printLog());
		
		// TODO remove close
		mapper.newState.close();
		
		return mapper.newState;

	}
		
	private void mapStates() {
		StoragePool<?,?> newPool;
		oldLbpoMap = new int[oldState.getTypes().size()];
		projectionOffsetMap = new int[oldState.getTypes().size()];
		
		int lbpo = 0;
		
		for(StoragePool<?, ?> oldPool : oldState.getTypes()) {
			// get name equivalent pool in new state
			newPool = newState.pool(oldPool.name());
			
			// if new pool is null, we want to project the type to one of its superpools
			// because of the type order we can take the mapping of the direct supertype
			// if we can not project the type, the mapping of the superpool will be null
			if(newPool == null) {
				newPool = poolMapping.get(oldPool.superPool);
				if(newPool != null) {
					// if we can project the old pool, we need the offset map later to refer to the types instances
					projectionOffsetMap[oldPool.typeID-32] = newPool.staticDataInstances;
					newPool.staticDataInstances += oldPool.staticDataInstances;
					MappingLog.genProjectionMessage(oldPool, newPool);
				} else {
					MappingLog.genTypeNotFoundError(oldPool);
				}
			} else {		
				newPool.staticDataInstances += oldPool.staticDataInstances;
			}
			
			poolMapping.put(oldPool, newPool);
			
			// create oldLbpoMap
			if(oldPool.superPool == null) {
				lbpo = 0;
			}
			oldLbpoMap[oldPool.typeID-32] = lbpo;
			lbpo += oldPool.staticDataInstances;
		}
	}
	
	private void transferData() throws InterruptedException {
		StoragePool.fixed(newState.getTypes());
		
		// calculate new lbpos
		newLbpoMap = new int[newState.getTypes().size()];
		int lbpo = 0;
		
		for(StoragePool<?,?> newPool : newState.getTypes()) {
			if(newPool.superPool == null) {
				lbpo = 0;
			}
			newLbpoMap[newPool.typeID-32] = lbpo;
			newPool.blocks().add(new Block(lbpo, newPool.size(), newPool.staticDataInstances));
			lbpo += newPool.staticDataInstances;
		}
	
		// allocate new skillobjects
		final Semaphore barrier = new Semaphore(0, false);
		int reads = 0;
		for(StoragePool<?,?> newPool : newState.getTypes()) {
			if(newPool instanceof BasePool<?>) {
				reads += ((BasePool<?>) newPool).performAllocations(barrier);
			}
		}
		
		barrier.acquire(reads);
		
		// set default values
		for(StoragePool<?,?> newPool : newState.getTypes()) {
			StaticFieldIterator fit = newPool.fields();
			while(fit.hasNext()) {
				FieldUtils.setDefaultValues(fit.next());
			}
		}
		
		StoragePool.unfix(newState.getTypes());
		
		FieldCompatibilityChecker checker = new FieldCompatibilityChecker(this);
		FieldIterator oldFieldIt;
		FieldDeclaration<?,?> oldField;
		FieldDeclaration<?,?> newField;
	
		for(StoragePool<?,?> oldPool : oldState.getTypes()) {
			// get mapped pool, if not null we want to transfer the fields
			StoragePool<?,?> newPool = poolMapping.get(oldPool);
			if(newPool != null) {
				
				// iterate over all fields of the old type
				oldFieldIt = oldPool.allFields();
				while(oldFieldIt.hasNext()) {
					oldField = oldFieldIt.next();
					
					// skip constant fields
					if(oldField.type().typeID > 4) {
						
						// search for right field in fields of new type
						newField = searchField(oldField.name(), newPool);
						
						if(newField != null) {
							if(checker.fieldsCompatible(oldField, newField)) transferFieldData(oldField, newField, oldPool, newPool);
							else {
								MappingLog.genFieldIncompatibleError(oldField, newField);
							}
						} else {
							if(newPool.size() > 0) MappingLog.genFieldNotFoundError(oldField);
						}
					}
				}
			}
		}
	}

	private FieldDeclaration<?,?> searchField(String fieldname, StoragePool<?, ?> newPool) {
		FieldIterator newFieldIt = newPool.allFields();
		FieldDeclaration<?, ?> newField;

		while(newFieldIt.hasNext()) {
			newField = newFieldIt.next();
			if(newField.name().equals(fieldname)) return newField;		
		}
		
		return null;
	}

	private Object castNumber(int id, Number n) {
		switch(id) {
		case 7: return n.byteValue();
		case 8: return n.shortValue();
		case 9: return n.intValue();
		case 10: 
		case 11:
			return n.longValue();
		case 12: return n.floatValue();
		case 13: return n.doubleValue();
		default:
			// should never happen
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	private <T> void transferFieldData(FieldDeclaration<?, ?> oldField, FieldDeclaration<T, ?> newField,
			StoragePool<?,?> oldPool, StoragePool<?,?> newPool) {
		int id = newField.type().typeID;
		if(id >= 32 || id == 5) {
			for(SkillObject oldObj : oldPool) {
				SkillObject newObj = calculateNewSkillObject(oldObj);
				
				// user types
				SkillObject value = calculateNewSkillObject((SkillObject)oldField.get(oldObj));
				newField.set(newObj,(T)value);
			}
		} else if(6 == id || 14 == id) { 
			for(SkillObject oldObj : oldPool) {
				SkillObject newObj = calculateNewSkillObject(oldObj);
				
				// string and bool
				newField.set(newObj, (T)oldField.get(oldObj));
			}
		} else if(7 <= id && id <= 13){
			for(SkillObject oldObj : oldPool) {
				SkillObject newObj = calculateNewSkillObject(oldObj);
				
				// number types
				Object value = castNumber(id, (Number) oldField.get(oldObj));

				newField.set(newObj, (T)value);
			}
		} else if(15 <= id && id <= 19) {
			for(SkillObject oldObj : oldPool) {
				SkillObject newObj = calculateNewSkillObject(oldObj);
				
				// linear collections
				Collection<?> oldValues = (Collection<?>)oldField.get(oldObj);
				Collection<Object> newValues = null;
				switch(id) {
				case 15:
				case 17:
					newValues = new ArrayList<>(oldValues.size());
					break;
				case 18:
					newValues = new LinkedList<>();
					break;
				case 19:
					newValues = new HashSet<>(oldValues.size());
					break;
				}
				
				for(Object o : oldValues) {
					if(o instanceof SkillObject) newValues.add((Object)calculateNewSkillObject((SkillObject)o));
					else if(o instanceof Number) newValues.add(castNumber(((SingleArgumentType<?, ?>)newField.type()).groundType.typeID, (Number) o));
					else newValues.add(o);
				}
				
				newField.set(newObj, (T)newValues);
			}
		} else if(20 == id) {
			for(SkillObject oldObj : oldPool) {
				SkillObject newObj = calculateNewSkillObject(oldObj);
				
				// map
				Map<?,?> oldMap = (Map<?,?>)oldField.get(oldObj);
				newField.set(newObj, (T)createNewMap(oldMap, ((MapType<?,?>)newField.type()).keyType, ((MapType<?,?>)newField.type()).valueType));
			}
		}
	}
	
	private Map<Object,Object> createNewMap(Map<?,?> oldMap, FieldType<?> newKeyType, FieldType<?> newValueType) {
		Map<Object,Object> newMap = new HashMap<>();
		Object value;
		Object key;
		for(Entry<?, ?> oldEntry : oldMap.entrySet()) {
			key = oldEntry.getKey();
			if(key instanceof SkillObject) key = calculateNewSkillObject((SkillObject) key);
			else if(key instanceof Number) key = castNumber(newKeyType.typeID, (Number) key);
			
			value = oldEntry.getValue();
			if(value instanceof Map<?,?>) value = createNewMap((Map<?,?>)value, ((MapType<?,?>)newValueType).keyType, ((MapType<?,?>)newValueType).valueType);
			else if(value instanceof SkillObject) value = calculateNewSkillObject((SkillObject) value);
			else if(value instanceof Number) value = castNumber(newValueType.typeID, (Number) value);
			
			newMap.put(key, value);
		}
		
		return newMap;
	}
	
	protected SkillObject calculateNewSkillObject(SkillObject oldObject) {
		if(oldObject == null) return null;
		StoragePool<?,?> oldPool = oldState.pool(oldObject.skillName());
		StoragePool<?,?> newPool = poolMapping.get(oldPool);
		if(newPool == null) return null;
		
		// skill id - lbpo(oldPool) => relative id in oldPool
		// relative id in oldPool + lbpo(newPool) => relative id in new pool
		// add projection offset if newPool is now part of another pool (projection)
		int id = oldObject.getSkillID() - oldLbpoMap[oldPool.typeID-32]
				+ newLbpoMap[newPool.typeID-32] + projectionOffsetMap[oldPool.typeID-32];

		return newPool.getByID(id);
	}
	
	class ErrorLog {
		private List<String> messages = new ArrayList<>();
		
		public void generateTypeNotFoundError(StoragePool<?, ?> oldType) {
			messages.add("Error: Type " + oldType + " not found");
		}

		public void generateFieldNotFoundError(StoragePool<?, ?> oldType, StoragePool<?, ?> newType, 
				FieldDeclaration<?, ?> oldField) {
			StringBuilder sb = new StringBuilder();
			sb.append("Error: Field ").append(oldField).append(" not found for Type Mapping ").append(oldType)
				.append(" -> ").append(newType).append(". ");
			if(oldType.size() == 0) sb.append("Error is less severe because there are no instances.");
			messages.add(sb.toString());
		}
		
		public void generateFieldIncompatibleError(StoragePool<?, ?> oldType, StoragePool<?, ?> newType, 
				FieldDeclaration<?, ?> oldField, FieldDeclaration<?, ?> newField) {
			StringBuilder sb = new StringBuilder();
			sb.append("Error: Field mapping ").append(oldField).append(" -> ").append(newField)
				.append(" of type Mapping ").append(oldType).append(" -> ").append(newType).append(" is not compatible");
			messages.add(sb.toString());
		}
		
		public void generateFieldMappingWarning(StoragePool<?, ?> oldType, StoragePool<?, ?> newType, 
				FieldDeclaration<?, ?> oldField, FieldDeclaration<?, ?> newField, String comment) {
			StringBuilder sb = new StringBuilder();
			sb.append("Warning generated by field Mapping ").append(oldField).append(" -> ").append(newField)
				.append(" of type Mapping ").append(oldType).append(" -> ").append(newType).append(":\n")
				.append("\t").append(comment);
			messages.add(sb.toString());
		}
		
		public void generateIntFloatWarning(FieldType<?> oldType, FieldType<?> newType, String comment) {
			StringBuilder sb = new StringBuilder();
			sb.append("Warning generated by type mapping ").append(oldType).append(" -> ").append(newType).append(":\n")
				.append("\t").append(comment);
			messages.add(sb.toString());
		}
		
		public void printMessages() {
			for(String s : messages) {
				System.out.println(s);
			}
		}
	}

}
