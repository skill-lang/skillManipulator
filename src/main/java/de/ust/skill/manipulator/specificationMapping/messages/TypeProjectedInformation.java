package de.ust.skill.manipulator.specificationMapping.messages;

import de.ust.skill.common.java.internal.StoragePool;

/**
 * Information for the user that a type is projected to one of his supertypes in the new typesystem.
 * 
 * @author olibroe
 *
 */
public class TypeProjectedInformation extends MappingInformation {
	public final StoragePool<?,?> oldPool;
	public final StoragePool<?,?> newPool;
	
	public TypeProjectedInformation(StoragePool<?,?> oldPool, StoragePool<?,?> newPool) {
		this.oldPool = oldPool;
		this.newPool = newPool;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof TypeProjectedInformation) {
			return this.oldPool.equals(((TypeProjectedInformation) o).oldPool);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return oldPool.hashCode();
	}

	@Override
	public String toString() {
		return new String("Type Projection: " + oldPool + " -> " + newPool);
	}

}
