package gcTest;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import common.CommonTest;
import de.ust.skill.skillManipulator.GarbageCollector;
import de.ust.skill.skillManipulator.GarbageCollector.CollectionRoot;
import de.ust.skill.skillManipulator.SkillFile;

class SimpleImlTest extends CommonTest{
	
	@Test
	void testSimpleIml() throws Exception {
		Path path = tmpFile("simple.iml.gc");

        SkillFile sf = SkillFile.open("src/test/resources/simpleIml/simple.iml.sf");
        sf.changePath(path);
        
        Set<CollectionRoot> roots = new HashSet<>();
        roots.add(new CollectionRoot("imlgraph"));
        
        GarbageCollector.run(sf, roots, false, true, true);
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/simpleIml/simple.iml.gc.sf");
        compareSkillFiles(sfExpected, sf);
	}

	@Test
	void testDoubleRun() throws Exception {
		Path path = tmpFile("simple.iml.double.run");

        SkillFile sf = SkillFile.open("src/test/resources/simpleIml/simple.iml.sf");
        sf.changePath(path);
        
        Set<CollectionRoot> roots = new HashSet<>();
        roots.add(new CollectionRoot("imlgraph"));
        
        GarbageCollector.run(sf, roots, false, true, true);
        GarbageCollector.run(sf, roots, false, true, true);
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/simpleIml/simple.iml.gc.sf");
        compareSkillFiles(sfExpected, sf);
	}
	
	@Test
	void testEmpty() throws Exception {
		Path path = tmpFile("simple.iml.empty");

        SkillFile sf = SkillFile.open("src/test/resources/simpleIml/simple.iml.sf");
        sf.changePath(path);
        
        GarbageCollector.run(sf, null, false, true, true);

        sf.close();

        SkillFile sfExpected = SkillFile.open("src/test/resources/empty-file.sf");
        compareSkillFiles(sfExpected, sf);
        
        // empty SkillFile expected, therefore binary equality possible
        // empty SkillFile is 2 Bytes zeros
        Assertions.assertEquals(sha256(path), sha256(new File("src/test/resources/empty-file.sf").toPath()));
	}
}
