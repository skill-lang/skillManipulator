package de.ust.skill.manipulator.performance;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import de.ust.skill.manipulator.internal.SkillFile;
import de.ust.skill.manipulator.gc.CollectionRoot;
import de.ust.skill.manipulator.gc.GarbageCollector;

public class GcPerformance {

	public static void main(String[] args) throws Exception {
		SkillFile sf = SkillFile.open(args[0]);
		
		File r = File.createTempFile("gcPerformance",".sf");
		r.deleteOnExit();
        sf.changePath(r.toPath());
        
        Set<CollectionRoot> roots = new HashSet<>();
        for(int i = 1; i < args.length; i++) {
        	roots.add(new CollectionRoot(args[i]));
        }
        
        long startTime = System.nanoTime();
        
        GarbageCollector.run(sf, roots, false, false, false);
        
        long endTime = System.nanoTime();
        
        sf.close();

        System.out.print((endTime - startTime));
	}

}
