package de.ust.skill.skillManipulator;

import de.ust.skill.common.java.api.Access;
import de.ust.skill.common.java.internal.SkillObject;

public class TypeUtils {
	static void deleteType(SkillFile sf, Access<?> type) {
		System.out.println("deleting type '" + type.name() + "'");
		for(Access<?> t : sf.allTypes()) {
			if(t.equals(type)) {
				for(SkillObject o : t) {
					sf.delete(o);
				}
			}
		}
	}
	
	static void deleteType(SkillFile sf, String type) {
		Access<?> t = getType(sf, type);
		if(t != null) {
			deleteType(sf, t);
		}
	}
	
	static Access<?> getType(SkillFile sf, String type) {
		for(Access<?> t : sf.allTypes()) {
			if(t.name().equals(type)) {
				return t;
			}
		}
		return null;
	}

}
