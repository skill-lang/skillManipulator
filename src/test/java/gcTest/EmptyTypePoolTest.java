package gcTest;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import common.CommonTest;
import de.ust.skill.skillManipulator.GarbageCollector;
import de.ust.skill.skillManipulator.GarbageCollector.CollectionRoot;
import de.ust.skill.skillManipulator.SkillFile;

class RemoveStringTestCases extends CommonTest{

	@Test
	void testReference() throws Exception {
		Path path = tmpFile("ltgc.simple");

        SkillFile sf = SkillFile.open("src/test/resources/ltgc/simple.sf");
        sf.changePath(path);
        
        Set<CollectionRoot> roots = new HashSet<>();
        roots.add(new CollectionRoot("t"));
        
        GarbageCollector.run(sf, roots, false, true, true);
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/ltgc/result.sf");
        compareSkillFiles(sfExpected, sf);
	}
	
	@Test
	void testReferenceWithFlagSet() throws Exception {
		Path path = tmpFile("ltgc.simple");

        SkillFile sf = SkillFile.open("src/test/resources/ltgc/simple.sf");
        sf.changePath(path);
        
        Set<CollectionRoot> roots = new HashSet<>();
        roots.add(new CollectionRoot("t"));
        
        GarbageCollector.run(sf, roots, true, true, true);
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/ltgc/result.sf");
        compareSkillFiles(sfExpected, sf);
	}
	
	@Test
	void testArray() throws Exception {
		Path path = tmpFile("ltgca.simple");

        SkillFile sf = SkillFile.open("src/test/resources/ltgca/simple.sf");
        sf.changePath(path);
        
        Set<CollectionRoot> roots = new HashSet<>();
        roots.add(new CollectionRoot("t"));
        
        GarbageCollector.run(sf, roots, false, true, true);
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/ltgca/result_without.sf");
        compareSkillFiles(sfExpected, sf);
	}
	
	@Test
	void testMap() throws Exception {
		Path path = tmpFile("ltgcm.simple");

        SkillFile sf = SkillFile.open("src/test/resources/ltgcm/simple.sf");
        sf.changePath(path);
        
        Set<CollectionRoot> roots = new HashSet<>();
        roots.add(new CollectionRoot("t"));
        
        GarbageCollector.run(sf, roots, false, true, true);
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/ltgcm/result_without.sf");
        compareSkillFiles(sfExpected, sf);
	}
	
	@Test
	void testArrayStays() throws Exception {
		Path path = tmpFile("ltgca.simple.stays");

        SkillFile sf = SkillFile.open("src/test/resources/ltgca/simple.sf");
        sf.changePath(path);
        
        Set<CollectionRoot> roots = new HashSet<>();
        roots.add(new CollectionRoot("t"));
        
        GarbageCollector.run(sf, roots, true, true, true);
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/ltgca/simple.sf");
        compareSkillFiles(sfExpected, sf);
	}
	
	@Test
	void testMapStays() throws Exception {
		Path path = tmpFile("ltgcm.simple.stays");

        SkillFile sf = SkillFile.open("src/test/resources/ltgcm/simple.sf");
        sf.changePath(path);
        
        Set<CollectionRoot> roots = new HashSet<>();
        roots.add(new CollectionRoot("t"));
        
        GarbageCollector.run(sf, roots, true, true, true);
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/ltgcm/simple.sf");
        compareSkillFiles(sfExpected, sf);
	}

}
