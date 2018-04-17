package de.ust.skill.skillManipulator;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import de.ust.skill.common.java.api.Access;
import de.ust.skill.common.java.api.SkillException;
import de.ust.skill.common.java.api.SkillFile.Mode;
import de.ust.skill.common.java.internal.SkillObject;

public class Main {

	public static void main(String[] args) {
	    try {
	    	SkillFile sf = SkillFile.open("/media/olibroe/DATA/Ubuntu_Data/imlfiles/simple.iml.sf", Mode.Read);
//	    	SkillFile sf = SkillFile.open("/media/olibroe/DATA/Ubuntu_Data/gitlab/skill/testsuites/java/test/age.sf", Mode.Read);
	    	//SkillFile sf = SkillFile.open("/media/olibroe/DATA/Ubuntu_Data/gitlab/skill/testsuites/java/src/test/resources/date-example-with-empty-age-pool.sf", Mode.Read);
	    	
	    	Set<String> rootTypes = new HashSet<>();
			rootTypes.add("imlgraph");
			Set<Integer> rootIds = new HashSet<>();
			//rootIds.add(2);
	    	
//			for(Access<?> t : sf.allTypes()) {
//				for(SkillObject o : t) {
//					System.out.println(o);
//				}
//			}
			
			//TypeUtils.deleteType(sf, "age");
			GarbageCollector.run(sf, rootTypes, rootIds, true, true);
	    	
//	    	for(Access<?> t : sf.allTypes()) {
//				for(SkillObject o : t) {
//					System.out.println(o.toString());
//				}
//			}
	    } catch (SkillException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
