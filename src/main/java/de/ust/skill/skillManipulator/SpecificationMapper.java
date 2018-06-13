package de.ust.skill.skillManipulator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

import de.ust.skill.common.java.internal.BasePool;
import de.ust.skill.common.java.internal.FieldDeclaration;
import de.ust.skill.common.java.internal.FieldIterator;
import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.common.java.internal.StaticFieldIterator;
import de.ust.skill.common.java.internal.StoragePool;
import de.ust.skill.common.java.internal.parts.Block;
import de.ust.skill.ir.TypeContext;

public class SpecificationMapper {
	
	Map<StoragePool<?,?>, StoragePool<?,?>> poolMapping = new HashMap<>();
	
	private static SkillState newState;
	private static SkillState oldState;
	
	int[] lbpoMap;
	int[] oldLbpoMap;
	int[] newPoolOffsetMap;
	
	private SpecificationMapper() {
		
	}
	
	public static void map(TypeContext tc, SkillFile sf, Path targetPath) {
		SpecificationMapper mapper = new SpecificationMapper();
		
		oldState = (SkillState)sf;
		
		try {
			newState = StateCreator.createNewState(tc, targetPath);
		} catch (IOException e) {
			// TODO can not create state, abort
			e.printStackTrace();
		}
		
		mapper.mapStates();
		
		mapper.transferData();
		
		newState.prettyPrint();
		
		newState.close();

	}
		
	private void mapStates() {
		StoragePool<?,?> newPool;
		oldLbpoMap = new int[oldState.getTypes().size()];
		newPoolOffsetMap = new int[oldState.getTypes().size()];
		
		int lbpo = 0;
		
		for(StoragePool<?, ?> oldPool : oldState.getTypes()) {
			newPool = newState.pool(oldPool.name());
			if(newPool == null) {
				// TODO type does not exist anymore
				StoragePool<?, ?> superPool = poolMapping.get(oldPool.superPool);
				poolMapping.put(oldPool, superPool);
				if(superPool != null) {
					newPoolOffsetMap[oldPool.typeID-32] = superPool.staticDataInstances;
					superPool.staticDataInstances += oldPool.staticDataInstances;
				}
			} else {
				poolMapping.put(oldPool, newPool);
				newPool.staticDataInstances += oldPool.staticDataInstances;
			}
			
			// create oldLbpoMap
			if(oldPool.superPool == null) {
				lbpo = 0;
			}
			oldLbpoMap[oldPool.typeID-32] = lbpo;
			lbpo += oldPool.staticDataInstances;
		}
	}
	
	private void transferData() {
		StoragePool.fixed(newState.getTypes());
		
		lbpoMap = new int[newState.getTypes().size()];
		int lbpo = 0;
		
		for(StoragePool<?,?> newPool : newState.getTypes()) {
			if(newPool.superPool == null) {
				lbpo = 0;
			}
			lbpoMap[newPool.typeID-32] = lbpo;
			newPool.blocks().add(new Block(lbpo, newPool.size(), newPool.staticDataInstances));
			lbpo += newPool.staticDataInstances;
		}
	
		final Semaphore barrier = new Semaphore(0, false);
		int reads = 0;
		for(StoragePool<?,?> newPool : newState.getTypes()) {
			if(newPool instanceof BasePool<?>) {
				reads += ((BasePool<?>) newPool).performAllocations(barrier);
			}
		}
		try {
			barrier.acquire(reads);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// set default values
		for(StoragePool<?,?> newPool : newState.getTypes()) {
			StaticFieldIterator fit = newPool.fields();
			while(fit.hasNext()) {
				FieldUtils.setDefaultValues(fit.next());
			}
		}
		
		StoragePool.unfix(newState.getTypes());
		
		for(StoragePool<?,?> oldPool : oldState.getTypes()) {
			StoragePool<?,?> newPool = poolMapping.get(oldPool);
			if(newPool != null) {
				FieldIterator oldFieldIt = oldPool.allFields();
				FieldIterator newFieldIt;

				FieldDeclaration<?,?> oldField;
				FieldDeclaration<?,?> newField;
				boolean found;
				while(oldFieldIt.hasNext()) {
					oldField = oldFieldIt.next();
					newFieldIt = newPool.allFields();
					found = false;
					while(newFieldIt.hasNext() && found == false) {
						newField = newFieldIt.next();
						// TODO comparison of types
						if(newField.name().equals(oldField.name())) {
							transferFieldData(oldField, newField, oldPool, newPool);
							found = true;
						}
					}
					if(found == false) {
						// TODO differ projection and normal field not found
						System.out.println("Field " + oldField.name() + " of old type " + oldField.type().toString() + " for type " + oldPool.name() + " not found");
						if(oldPool.staticSize() == 0) {
							System.out.println("But type has no instances.");
						}
					}
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private <T> void transferFieldData(FieldDeclaration<?, ?> oldField, FieldDeclaration<T, ?> newField,
			StoragePool<?,?> oldPool, StoragePool<?,?> newPool) {
		int id = newField.type().typeID;
		if(id >= 32) {
			for(SkillObject oldObj : oldPool) {
				SkillObject newObj = calculateNewSkillObject(oldObj);
				
				// user types
				SkillObject value = calculateNewSkillObject((SkillObject)oldField.get(oldObj));
				newField.set(newObj,(T)value);
			}
		} else if(6 <= id && id <= 14){
			for(SkillObject oldObj : oldPool) {
				SkillObject newObj = calculateNewSkillObject(oldObj);
				
				// ground types
				newField.set(newObj, (T)oldField.get(oldObj));
			}
		} else if(15 <= id && id <= 19) {
			for(SkillObject oldObj : oldPool) {
				SkillObject newObj = calculateNewSkillObject(oldObj);
				
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
					else newValues.add(o);
				}
				newField.set(newObj, (T)newValues);
			}
		} else if(20 == id) {
			for(SkillObject oldObj : oldPool) {
				SkillObject newObj = calculateNewSkillObject(oldObj);
				
				Map<?,?> oldMap = (Map<?,?>)oldField.get(oldObj);
				newField.set(newObj, (T)createNewMap(oldMap));
			}
		}
	}
	
	private Map<Object,Object> createNewMap(Map<?,?> oldMap) {
		Map<Object,Object> newMap = new HashMap<>();
		Object value;
		Object key;
		for(Entry<?, ?> oldEntry : oldMap.entrySet()) {
			key = oldEntry.getKey();
			if(key instanceof SkillObject) key = calculateNewSkillObject((SkillObject) key);
			
			value = oldEntry.getValue();
			if(value instanceof Map<?,?>) value = createNewMap((Map<?,?>)value);
			else if(value instanceof SkillObject) value =    calculateNewSkillObject((SkillObject) value);
			
			newMap.put(key, value);
		}
		
		return newMap;
	}
	
	private SkillObject calculateNewSkillObject(SkillObject oldObject) {
		StoragePool<?,?> oldPool = oldState.pool(oldObject.skillName());
		StoragePool<?,?> newPool = poolMapping.get(oldPool);
		int relativePoolOffset = oldObject.getSkillID() - oldLbpoMap[oldPool.typeID-32];
		
		// if pool is directly mapped, relative offset sufficient
		if(newPool.name().equals(oldPool.name())) {
			return newPool.getByID(relativePoolOffset + lbpoMap[newPool.typeID-32]);
		} else {
			return newPool.getByID(relativePoolOffset + lbpoMap[newPool.typeID-32] + newPoolOffsetMap[oldPool.typeID-32]);
		}
	}

}
