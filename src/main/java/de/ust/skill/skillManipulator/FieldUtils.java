package de.ust.skill.skillManipulator;

import de.ust.skill.common.java.internal.FieldDeclaration;
import de.ust.skill.common.java.internal.StoragePool;

public class FieldUtils {

	public static void reorderFields(StoragePool<?,?> type) {
		int nextID = 1;
		for (FieldDeclaration<?, ?> f : type.dataFields) {
			f.index = nextID;
			++nextID;
		}
	}
	
	public static boolean removeField(SkillFile sf, String fieldname, String ofType) {
		SkillState state = (SkillState)sf;
		StoragePool<?,?> type = state.pool(ofType);
		if(type == null) return false;
		
		return removeField(fieldname, type);
	}

	public static boolean removeField(String fieldname, StoragePool<?, ?> type) {
		boolean foundField = false;
		for(int i = 0; i < type.dataFields.size(); i++) {
			if(type.dataFields.get(i).name().equals(fieldname)) {
				type.dataFields.remove(i);
				foundField = true;
			}
		}
		
		if(foundField) {
			reorderFields(type);
			return true;
		}
		return false;
	}
	
	public static void removeAllFieldsOfType(SkillFile sf, int typeID) {
		SkillState state = (SkillState)sf;
		
		for(StoragePool<?, ?> t : state.getTypes()) {
			boolean foundField = false;
			for(int i = 0; i < t.dataFields.size(); i++) {
				if(t.dataFields.get(i).type().typeID == typeID) {
					t.dataFields.remove(i);
					foundField = true;
				}
			}
			if(foundField) reorderFields(t);
		}

	}
}
