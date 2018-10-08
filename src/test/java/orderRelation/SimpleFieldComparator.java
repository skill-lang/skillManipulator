package orderRelation;

import de.ust.skill.common.java.api.FieldDeclaration;
import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.manipulator.orderRelation.SkillObjectComparator;

/**
 * SkillObjectComparator that sorts SKillObjects after the given field if this is possible.
 * 
 * @author olibroe
 *
 * @param <T>
 */
public class SimpleFieldComparator<T> extends SkillObjectComparator {
	private final FieldDeclaration<T> f;
	
	public SimpleFieldComparator(FieldDeclaration<T> f) throws Exception {
		this.f = f;
		int typeID = f.type().typeID();
		if(typeID < 6 || typeID > 14) throw new Exception("Field values are not comparable.");
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public int compare(SkillObject o1, SkillObject o2) {
		Object value1 = f.get(o1);
		Object value2 = f.get(o2);

		if(value1 == value2) return 0;
		if(value1 == null) return 1;
		if(value2 == null) return -1;
		
		return ((Comparable<T>) value1).compareTo((T) value2);
	}

}
