package de.ust.skill.manipulator.specificationMapping.messages;

import de.ust.skill.common.java.internal.FieldDeclaration;
import de.ust.skill.common.java.internal.StoragePool;

/**
 * Information for the user that a field is not found in new typesystem.
 * 
 * @author olibroe
 *
 */
public class FieldNotFoundInformation extends MappingInformation {
	public final FieldDeclaration<?, ?> oldField;
	public final StoragePool<?,?> oldPool;
	public final StoragePool<?,?> newPool;
	
	public FieldNotFoundInformation(FieldDeclaration<?, ?> oldField, StoragePool<?,?> oldPool,
			StoragePool<?,?> newPool) {
		this.oldField = oldField;
		this.oldPool = oldPool;
		this.newPool = newPool;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof FieldNotFoundInformation) {
			return this.oldField.equals(((FieldNotFoundInformation) o).oldField) &&
					this.oldPool.equals(((FieldNotFoundInformation) o).oldPool);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return oldField.hashCode() ^ oldPool.hashCode();
	}

	@Override
	public String toString() {
		return new String("Field Missing: " + oldField + " (caused by mapping " + oldPool + " -> " + 
				newPool + ")");
	}

}
