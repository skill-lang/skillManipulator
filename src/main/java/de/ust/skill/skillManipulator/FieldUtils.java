package de.ust.skill.skillManipulator;

import de.ust.skill.common.java.internal.FieldDeclaration;
import de.ust.skill.common.java.internal.FieldIterator;
import de.ust.skill.common.java.internal.StoragePool;

/**
 * Class with methods to remove fields and reorder them.
 *
 * @author olibroe
 *
 */
public class FieldUtils {

	/**
	 * Reorders fields after a field was removed.
	 *
	 * @param type - type, which fields need to be reordered
	 */
	public static void reorderFields(StoragePool<?,?> type) {
		int nextID = 1;
		for (FieldDeclaration<?, ?> f : type.dataFields) {
			f.index = nextID;
			++nextID;
		}
	}
	
	/**
	 * This method deletes the field with the given fieldname of the given type.
	 * If the field is not found in the given type or the type is not found the method returns false.
	 * The return value is true if the removal was successful.
	 *
	 * @param sf - Skillfile
	 * @param fieldname - name of field to remove
	 * @param ofType - name of type
	 * @return - true if successful, otherwise false
	 */
	public static boolean removeField(SkillFile sf, String fieldname, String ofType) {
		SkillState state = (SkillState)sf;
		StoragePool<?,?> type = state.pool(ofType);
		if(type == null) return false;
		
		return removeField(fieldname, type);
	}

	/**
	 * This method deletes the field with the given fieldname of the given type.
	 * If the field is not found in the given type the method returns false. The return
	 * value is true if the removal was successful.
	 *
	 * @param fieldname - name of the field to remove
	 * @param type - type in which the field is located
	 * @return - true if successful, otherwise false
	 */
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

		System.out.println("Field " + fieldname + " not found in type " + type.name());
		FieldIterator fit = type.allFields();
		while(fit.hasNext()) {
			FieldDeclaration<?, ?> f = fit.next();
			if(f.name().equals(fieldname)) {
				System.out.println("Did you mean type " + f.owner().name() + "?");
			}
		}

		return false;
	}
	
	/**
	 * Removes all fields of the type with the given typeID.
	 *
	 * @param sf - Skillfile
	 * @param typeID - typeID of the type to remove
	 */
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
