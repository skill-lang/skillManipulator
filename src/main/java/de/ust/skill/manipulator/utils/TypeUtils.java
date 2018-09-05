package de.ust.skill.skillManipulator.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import de.ust.skill.common.java.internal.StaticDataIterator;
import de.ust.skill.common.java.internal.StoragePool;
import de.ust.skill.common.java.internal.TypeHierarchyIterator;
import de.ust.skill.skillManipulator.internal.SkillFile;
import de.ust.skill.skillManipulator.internal.SkillState;

/**
 * Util-class provides static methods for types
 * 
 * @author olibroe
 *
 */
public final class TypeUtils {
	/**
	 * No instantiation of utils class
	 */
	private TypeUtils() {};
	
	/**
	 * After removing or adding a type to the state a recreation of type IDs is necessary.
	 * Type IDs start at 32.
	 * 
	 * @param state - types of this state need a new ID
	 */
	public static void renewTypeIDs(SkillState state) {
		ArrayList<StoragePool<?, ?>> types = state.getTypes();
		int nextID = 32;
		for (StoragePool<?, ?>  s : types) {
			s.typeID = nextID;
			++nextID;
			s.setNextPool(null);
		}
		
		StoragePool.establishNextPools(types);
	}

	/**
	 * Deletes a type and all of its subtypes from the state.
	 * The steps are:
	 * 1. delete all objects of the type
	 * 2. identify all subtypes
	 * 3. delete all fields that have references on the type and its subtypes
	 * 4. remove type and its subtypes from state
	 * 5. renew typeIDs and next pointers
	 * 
	 * @param state
	 * @param type
	 */
	public static boolean deleteType(SkillState state, StoragePool<?,?> type) {
		ArrayList<StoragePool<?, ?>> types = state.getTypes();

		// add all subtypes to remove them too and delete their objects
		Set<StoragePool<?, ?>> deleteTypes = new HashSet<>();
		TypeHierarchyIterator<?, ?> it = new TypeHierarchyIterator<>(type);
		StaticDataIterator<?> sit;
		while(it.hasNext()) {
			StoragePool<?, ?> pool = it.next();
			deleteTypes.add(pool);
			sit = pool.staticInstances();
			while(sit.hasNext()) pool.delete(sit.next());
		}

		// delete all fields of the types we want to delete
		for(StoragePool<?, ?> t : deleteTypes)
			FieldUtils.removeAllFieldsOfType(state, t.typeID);

		// removes types we want to remove
		boolean successfullRemove = types.removeAll(deleteTypes);

		renewTypeIDs(state);
		
		return successfullRemove;
	}

	/**
	 * Wrapper function for convenience.
	 * Type can be given as a string here.
	 * 
	 * @param sf - SkillFile from which the type needs to be removed
	 * @param type - string of type name
	 * @return
	 */
	public static boolean deleteType(SkillFile sf, String type) {
		SkillState state = (SkillState)sf;
		StoragePool<?, ?> pool = state.pool(type);

		if(pool == null) {
			return false;
		} else {
			return deleteType(state, pool);
		}
	}

}
