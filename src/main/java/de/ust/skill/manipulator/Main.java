package de.ust.skill.skillManipulator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import de.ust.skill.common.java.api.SkillException;
import de.ust.skill.skillManipulator.GarbageCollector.CollectionRoot;

public class Main {

	public static void main(String[] args) {
	    try {	    	
//	    	SkillFile sf = SkillFile.open("/tmp/bash.iml.sf");
	    	SkillFile sf = SkillFile.open("src/test/resources/performance/gnugo.iml.sf");
//	    	SkillFile sf = SkillFile.open("/media/olibroe/DATA/Ubuntu_Data/gitlab/skill/testsuites/java/test/age.sf", Mode.Read);
	    	//SkillFile sf = SkillFile.open("/media/olibroe/DATA/Ubuntu_Data/gitlab/skill/testsuites/java/src/test/resources/date-example-with-empty-age-pool.sf", Mode.Read);
	    	
	    	Path path = new File("/tmp/test.sf").toPath();
	    	sf.changePath(path);
	    	
	    	System.out.println(Runtime.getRuntime().maxMemory());
	    	System.out.println(Runtime.getRuntime().totalMemory());
	    	
	    	Set<CollectionRoot> roots = new HashSet<>();
			roots.add(new CollectionRoot("imlgraph"));
			
	    	//System.out.println(sf.Strings().size());
//			System.out.println("Press enter");
//			Scanner scanner = new Scanner(System.in);
//			scanner.next();
//			scanner.close();
	    	
			//TypeUtils.deleteType(sf, "age");
	    	
			GarbageCollector.run(sf, roots, false, true, false);
//			
//			sf.close();
			
	    } catch (SkillException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
