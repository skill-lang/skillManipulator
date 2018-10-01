package de.ust.skill.manipulator.performance;

import java.io.File;

import de.ust.skill.manipulator.internal.SkillFile;
import de.ust.skill.manipulator.utils.TypeUtils;

public class RemoveTypePerformance {

	public static void main(String[] args) throws Exception {
		SkillFile sf = SkillFile.open(args[0]);
		
		File r = File.createTempFile("removeTypePerformance",".sf");
		r.deleteOnExit();
        sf.changePath(r.toPath());
        
        long startTime = System.nanoTime();
        
        TypeUtils.deleteType(sf, args[1]);
        
        long endTime = System.nanoTime();
        
        sf.close();

        System.out.print((endTime - startTime));

	}

}
