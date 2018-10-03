package de.ust.skill.manipulator.specificationMapping.messages;

import de.ust.skill.common.java.internal.FieldDeclaration;
import de.ust.skill.common.java.internal.StoragePool;

/**
 * Information for the user that two fields are incompatible.
 * 
 * @author olibroe
 *
 */
public class FieldIncompatibleInformation extends MappingInformation {
	public final FieldDeclaration<?, ?> oldField;
	public final FieldDeclaration<?, ?> newField;
	public final StoragePool<?,?> oldPool;
	public final StoragePool<?,?> newPool;
	
	public FieldIncompatibleInformation(FieldDeclaration<?, ?> oldField, FieldDeclaration<?, ?> newField,
			StoragePool<?,?> oldPool, StoragePool<?,?> newPool) {
		this.oldField = oldField;
		this.newField = newField;
		this.oldPool = oldPool;
		this.newPool = newPool;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof FieldIncompatibleInformation) {
			return this.oldField.equals(((FieldIncompatibleInformation) o).oldField) &&
					this.newField.equals(((FieldIncompatibleInformation) o).newField);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return oldField.hashCode() ^ newField.hashCode();
	}

	@Override
	public String toString() {
		return new String("Field mapping " + oldField + " -> " + newField + " not compatible (caused by type mapping " +
				oldPool + " -> " + newPool + ")");
	}

}
