package de.ust.skill.skillManipulator;

import java.util.ArrayList;

import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.common.java.internal.StoragePool;

public class TypeUtils {
	static void deleteType(SkillFile sf, StoragePool<?,?> type) {
		// delete all objects of type
		for(SkillObject o : type) {
			sf.delete(o);
		}
		
		// reorder types to remove type from list
		reorderTypes((SkillState) sf);
	}
	
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

	static boolean deleteType(SkillFile sf, String type) {
		StoragePool<?, ?> pool = ((SkillState)sf).pool(type);
		if(pool == null) {
			return false;
		} else {
			deleteType((SkillState) sf, pool);
			return true;
		}
	}

}
