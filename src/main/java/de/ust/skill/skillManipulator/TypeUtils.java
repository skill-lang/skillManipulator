package de.ust.skill.skillManipulator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.common.java.internal.StoragePool;

public class TypeUtils {
	
	public static void reorderTypes(SkillState state) {
		ArrayList<StoragePool<?, ?>> types = state.getTypes();
		int nextID = 32;
		for (StoragePool<?, ?>  s : types) {
			s.typeID = nextID;
			++nextID;
			s.setNextPool(null);
		}
		
		StoragePool.establishNextPools(types);
	}

	public static void deleteType(SkillState state, StoragePool<?,?> type) {
		ArrayList<StoragePool<?, ?>> types = state.getTypes();

		Set<StoragePool<?, ?>> deleteTypes = new HashSet<>();
		deleteTypes.add(type);

		// delete all objetcs of type
		for(SkillObject o : type) {
			state.delete(o);
		}

		// add all subtypes to remove them too
		// note: type order is important here
		for(StoragePool<?, ?> t : types) {
			if(deleteTypes.contains(t.superPool)) {
				deleteTypes.add(t);
			}
		}

		// delete all fields of the types we want to delete
		for(StoragePool<?, ?> t : deleteTypes)
			FieldUtils.removeAllFieldsOfType(state, t.typeID);

		// removes types we want to remove
		types.removeAll(deleteTypes);

		reorderTypes(state);
	}


	public static boolean deleteType(SkillFile sf, String type) {
		SkillState state = (SkillState)sf;
		StoragePool<?, ?> pool = state.pool(type);

		if(pool == null) {
			return false;
		} else {
			deleteType(state, pool);
			return true;
		}
	}

}
