package de.ust.skill.manipulator.specificationMapping.messages;

import de.ust.skill.common.java.internal.FieldDeclaration;

/**
 * Information for the user that two mapped fields produce a message.
 * 
 * @author olibroe
 *
 */
public class FieldMappingInformation extends MappingInformation {
	public final FieldDeclaration<?, ?> oldField;
	public final FieldDeclaration<?, ?> newField;
	public final String message;
	
	public FieldMappingInformation(FieldDeclaration<?, ?> oldField, FieldDeclaration<?, ?> newField, String message) {
		this.oldField = oldField;
		this.newField = newField;
		this.message = message;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof FieldMappingInformation) {
			return this.oldField.equals(((FieldMappingInformation) o).oldField) &&
					this.newField.equals(((FieldMappingInformation) o).newField);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return oldField.hashCode() ^ newField.hashCode();
	}

	@Override
	public String toString() {
		return new String("Field Mapping warning for " + oldField + " -> " + newField + ": " + message);
	}
}
