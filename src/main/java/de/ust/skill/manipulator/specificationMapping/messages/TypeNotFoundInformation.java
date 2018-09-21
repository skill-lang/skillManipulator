package de.ust.skill.manipulator.specificationMapping.messages;

import de.ust.skill.common.java.internal.StoragePool;

public class TypeNotFoundInformation extends MappingInformation{
	public final StoragePool<?,?> oldPool;
	
	public TypeNotFoundInformation(StoragePool<?,?> oldPool) {
		this.oldPool = oldPool;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof TypeNotFoundInformation) {
			return this.oldPool.equals(((TypeNotFoundInformation) o).oldPool);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return oldPool.hashCode();
	}

	@Override
	public String toString() {
		return new String("Type Missing: " + oldPool);
	}

}
