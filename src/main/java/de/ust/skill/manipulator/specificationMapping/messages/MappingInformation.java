package de.ust.skill.manipulator.specificationMapping.messages;

/**
 * Base class for all Mapping informations.
 * 
 * @author olibroe
 *
 */
public abstract class MappingInformation {
	
	public abstract boolean equals(Object o);
	public abstract int hashCode();
	
	public abstract String toString();
}
