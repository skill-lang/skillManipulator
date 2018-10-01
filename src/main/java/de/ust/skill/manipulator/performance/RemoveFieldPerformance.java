package de.ust.skill.manipulator.performance;

import java.io.File;

import de.ust.skill.manipulator.internal.SkillFile;
import de.ust.skill.manipulator.utils.FieldUtils;

public class RemoveFieldPerformance {

	public static void main(String[] args) throws Exception {
		SkillFile sf = SkillFile.open(args[0]);
		
		File r = File.createTempFile("removeFieldPerformance",".sf");
		r.deleteOnExit();
        sf.changePath(r.toPath());
        
        long startTime = System.nanoTime();
        
        FieldUtils.removeField(sf, args[1], args[2]);
        
        long endTime = System.nanoTime();
        
        sf.close();

        System.out.print((endTime - startTime));

	}

}
