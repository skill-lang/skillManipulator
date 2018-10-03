package de.ust.skill.manipulator.utils;

import java.util.ArrayList;
import java.util.Iterator;

import de.ust.skill.common.java.internal.FieldDeclaration;
import de.ust.skill.common.java.internal.FieldIterator;
import de.ust.skill.common.java.internal.FieldType;
import de.ust.skill.common.java.internal.StoragePool;
import de.ust.skill.common.java.internal.fieldTypes.ConstantLengthArray;
import de.ust.skill.common.java.internal.fieldTypes.MapType;
import de.ust.skill.common.java.internal.fieldTypes.SingleArgumentType;
import de.ust.skill.common.java.restrictions.DefaultValue;
import de.ust.skill.manipulator.OutputPrinter;
import de.ust.skill.manipulator.internal.SkillFile;
import de.ust.skill.manipulator.internal.SkillState;

/**
 * Utils-class provides static methods for fields
 *
 * @author olibroe
 *
 */
public final class FieldUtils {
	
	/**
	 * No instantiation of utils class
	 */
	private FieldUtils() {};
	
	/**
	 * Renews the indices of fields after a field has been removed.
	 *
	 * @param type - fields of this type need a new index
	 */
	public static void renewFieldIndices(StoragePool<?,?> type) {
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
	public static void removeField(SkillFile sf, String fieldname, String ofType) {
		SkillState state = (SkillState)sf;
		StoragePool<?,?> type = state.pool(ofType);
		if(type == null) return;
		
		removeField(fieldname, type);
	}

	/**
	 * This method deletes the field with the given fieldname of the given type.
	 *
	 * @param fieldname - name of the field to remove
	 * @param type - type in which the field is located
	 */
	public static void removeField(String fieldname, StoragePool<?, ?> type) {
		boolean foundField = false;
		Iterator<?> it = type.dataFields.iterator();
		while(it.hasNext()) {
			FieldDeclaration<?, ?> f = (FieldDeclaration<?,?>) it.next();
			// remove field if found and decrement the indices of all following fields
			if(f.name().equals(fieldname)) {
				it.remove();
				foundField = true;
			} else if(foundField) {
				f.index = f.index - 1;
			}
		}

		if(foundField) return;
		
		// Inform the user that the field is not found in the given type.
		// But maybe it could be found in one of the supertypes.
		OutputPrinter.println("Field " + fieldname + " not found in type " + type.name());
		FieldIterator fit = type.allFields();
		while(fit.hasNext()) {
			FieldDeclaration<?, ?> f = fit.next();
			if(f.name().equals(fieldname)) {
				OutputPrinter.println("Did you mean type " + f.owner().name() + "?");
			}
		}
	}
	
	/**
	 * Removes all fields of the type with the given typeID.
	 *
	 * @param sf - Skillfile
	 * @param typeID - typeID of the type to remove
	 */
	protected static void removeAllFieldsOfType(SkillFile sf, int typeID) {
		SkillState state = (SkillState)sf;
		
		// set to collect fields to delete
		for(StoragePool<?, ?> t : state.getTypes()) {
			int deletedFields = 0;
			Iterator<?> it = t.dataFields.iterator();
			
			while(it.hasNext()) {
				FieldDeclaration<?, ?> f = (FieldDeclaration<?,?>) it.next();
				
				// Remove all fields of given type and decrement all following files by the number of removed types
				// so far.
				if(fieldContainsType(typeID, f.type())) {
					it.remove();
					deletedFields += 1;
				} else {
					f.index = f.index - deletedFields;
				}
			}

		}

	}

	/**
	 * Check if the given type contains references or values of the given type-ID.
	 * 
	 * @param typeID - the type-ID that is contained or not
	 * @param type - the type for which the check is performed
	 * @return true if type contains references or values of the given type-ID
	 */
	private static boolean fieldContainsType(int typeID, FieldType<?> type) {
		int typeIdField = type.typeID;
		if(15 <= typeIdField && typeIdField <= 19) // linear collections
			return ((SingleArgumentType<?,?>)type).groundType.typeID == typeID;
		if(typeIdField == 20) // map type
			return fieldContainsType(typeID, ((MapType<?,?>)type).keyType) && fieldContainsType(typeID, ((MapType<?,?>)type).valueType);
		
		// ground types and user types
		return typeIdField == typeID;
	}
	
	/**
	 * Sets the default value for all objects that have the specified field.
	 * Default values are taken from (in this order):
	 * 1. Field default
	 * 2. Field-Type default (only user types)
	 * 3. Standard default
	 * 
	 * @param newField - default value is set for object that have this field
	 */
	public static Object getDefaultValue(FieldDeclaration<?, ?> newField) {
		// future work: if the implementation of default value is ready, supply default here

		Object value = null;
		
		// field default has priority
		DefaultValue<?> defaultValue = newField.defaultValue;
		
		// if type is user type there could be a type default
		if(defaultValue == null && newField.type().typeID >= 32) defaultValue = ((StoragePool<?,?>)newField.type()).defaultValue;
		
		// set default from field or type
		if(defaultValue != null) {
			value = defaultValue.getValue();
		}
		
		// if there is no default in field or type, set standard default
		if(value == null) value = getDefaultValue(newField.type());
		
		return value;
	}

	/**
	 * Provides standard default values as specified by Timm Felden.
	 * For further reading look at technical report "The SKilL Language V1.0".
	 * 
	 * @param type - the type you want the standard default value for
	 * @return standard default value for type
	 */
	public static Object getDefaultValue(FieldType<?> type) {
		Object value;
		int id = type.typeID;
		
		// take minimum data type to prevent class cast exception
		// bool
		if(6 == id) value = false;
		// integer types
		else if(7 <= id && id <= 11) value = (byte) 0;
		// float types
		else if(id == 12 || id == 13) value = (float)0.0;
		// constant length array
		else if(id == 15) {
			Object defGroundValue = getDefaultValue(((ConstantLengthArray<?>)type).groundType);
			ArrayList<Object> retval = new ArrayList<>();
			for(int i = 0; i < ((ConstantLengthArray<?>)type).length; i++) retval.add(defGroundValue);
			value = retval;
		}
		// others
		else value = null;
		
		return value;
	}
}
