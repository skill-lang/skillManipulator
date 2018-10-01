package de.ust.skill.manipulator.performance;

import de.ust.skill.common.java.api.Access;
import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.manipulator.internal.SkillFile;
import de.ust.skill.manipulator.internal.SkillState;

public class NodeCounter {
	
	public static void main(String[] args) throws Exception {
		SkillFile sf = SkillFile.open(args[0]);
		
		if(args.length < 2) {
			((SkillState)sf).collectStrings();
			long count = 0;
			for(Access<? extends SkillObject> t : sf.allTypes()) {
				if(t.superName() == null) count += t.size();
			}
			count += sf.Strings().size();
		
			System.out.print(count);
		} else {
			String type = args[1];
			for(Access<? extends SkillObject> t : sf.allTypes()) {
				if(t.name().equals(type)) System.out.println(t.size());
			}
		}
	}
}
