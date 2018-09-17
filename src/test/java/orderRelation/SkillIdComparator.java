package orderRelation;

import de.ust.skill.common.java.internal.FieldDeclaration;
import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.manipulator.orderRelation.SkillObjectComparator;

public class SkillIdComparator extends SkillObjectComparator {
	private final FieldDeclaration<?, ?> f;
	
	public SkillIdComparator(FieldDeclaration<?,?> f) throws Exception {
		if(f.type().typeID < 32) throw new Exception("Field is not reference type.");
		this.f = f;
	}
	
	@Override
	public int compare(SkillObject o1, SkillObject o2) {
		SkillObject obj1 = (SkillObject) f.get(o1);
		SkillObject obj2 = (SkillObject) f.get(o2);
		
		if(obj1 == null && obj2 == null) return 0;
		if(obj1 == null) return 1;
		if(obj2 == null) return -1;
	
		return ((Integer)obj1.getSkillID()).compareTo((Integer)obj2.getSkillID());
	}

}
