package de.ust.skill.manipulator.orderRelation;

import java.util.Comparator;

import de.ust.skill.common.java.internal.SkillObject;

/**
 * This class is the abstract base of a Comparator for Skill Objects.
 * 
 * @author olibroe
 *
 */
public abstract class SkillObjectComparator implements Comparator<SkillObject>{
	
	@Override
	public abstract int compare(SkillObject o1, SkillObject o2);
}
