package orderRelation;

import de.ust.skill.common.java.internal.FieldDeclaration;
import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.common.java.internal.StoragePool;
import de.ust.skill.manipulator.internal.SkillFile;
import de.ust.skill.manipulator.internal.SkillState;
import de.ust.skill.manipulator.orderRelation.SkillObjectComparator;

/**
 * SkillObjectComparator that sorts the SkillObjects after the field nodeid of type storable.
 * 
 * @author olibroe
 *
 */
public class NodeidComparator extends SkillObjectComparator{
	private FieldDeclaration<?, ?> nodeidField = null;
	
	protected NodeidComparator(SkillFile sf) {
		StoragePool<?,?> pool = ((SkillState)sf).pool("storable");
		for(FieldDeclaration<?, ?> f : pool.dataFields) {
			if(f.name().equals("nodeid")) nodeidField = f;
		}
		
		
	}

	@Override
	public int compare(SkillObject o1, SkillObject o2) {
		if(o1.equals(o2)) return 0;
		
		// i know the return value will be a long
		Long value1 = (Long) nodeidField.get(o1);
		Long value2 = (Long) nodeidField.get(o2);
		
		// both skillobjects are from the same type, due to implementation of the sorting algorithm
		// so either are both null or they have both values
		if(value1 == null && value2 == null) return 0;
		
		return value1.compareTo(value2);
	}

}
