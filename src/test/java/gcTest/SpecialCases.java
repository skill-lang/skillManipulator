package gcTest;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import common.CommonTest;
import de.ust.skill.manipulator.gc.CollectionRoot;
import de.ust.skill.manipulator.gc.GarbageCollector;
import de.ust.skill.manipulator.internal.SkillFile;

class SpecialCases extends CommonTest{

	@Test
	void testKeepCollection() throws Exception {
		Path path = tmpFile("simple.special.case.keep");

        SkillFile sf = SkillFile.open("src/test/resources/gcSpecialCases/specification.sf");
        sf.changePath(path);
        
        Set<CollectionRoot> roots = new HashSet<>();
        roots.add(new CollectionRoot("t"));
        
        GarbageCollector.run(sf, roots, true, true, true);
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/gcSpecialCases/specification.sf");
        SkillFile sfActual = SkillFile.open(path);
        compareSkillFiles(sfExpected, sfActual);
	}
	
	@Test
	void testSimple() throws Exception {
		Path path = tmpFile("simple.special.case");

        SkillFile sf = SkillFile.open("src/test/resources/gcSpecialCases/specification.sf");
        sf.changePath(path);
        
        Set<CollectionRoot> roots = new HashSet<>();
        roots.add(new CollectionRoot("t"));
        
        GarbageCollector.run(sf, roots, false, true, true);
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/gcSpecialCases/result.sf");
        SkillFile sfActual = SkillFile.open(path);
        compareSkillFiles(sfExpected, sfActual);
	}

}
