package gcTest;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import common.CommonTest;
import de.ust.skill.skillManipulator.gc.GarbageCollector;
import de.ust.skill.skillManipulator.gc.CollectionRoot;
import de.ust.skill.skillManipulator.internal.SkillFile;

class AgeTest extends CommonTest{
	
	@Test
	void testNothingChanged() throws Exception {
		Path path = tmpFile("age-example.nothing.changed");

        SkillFile sf = SkillFile.open("src/test/resources/age/age-example.sf");
        sf.changePath(path);
        
        Set<CollectionRoot> roots = new HashSet<>();
        roots.add(new CollectionRoot("age"));
        
        GarbageCollector.run(sf, roots, false, true, true);
   
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/age/age-example.sf");
        SkillFile sfActual = SkillFile.open(path);
        compareSkillFiles(sfExpected, sfActual);
	}
	
	@Test
	void testDeleteSecondObject() throws Exception {
		Path path = tmpFile("age-example.delete.second");

        SkillFile sf = SkillFile.open("src/test/resources/age/age-example.sf");
        sf.changePath(path);
        
        Set<CollectionRoot> roots = new HashSet<>();
        roots.add(new CollectionRoot("age",1));

        GarbageCollector.run(sf, roots, false, true, true);
        
        sf.close();

        SkillFile sfExpected = SkillFile.open("src/test/resources/age/age-example-delete-second.sf");
        SkillFile sfActual = SkillFile.open(path);
        compareSkillFiles(sfExpected, sfActual);
	}
	
	@Test
	void testDeleteFirstObject() throws Exception {
		Path path = tmpFile("age-example.delete.first");

        SkillFile sf = SkillFile.open("src/test/resources/age/age-example.sf");
        sf.changePath(path);
        
        Set<CollectionRoot> roots = new HashSet<>();
        roots.add(new CollectionRoot("age",2));

        GarbageCollector.run(sf, roots, false, true, true);
        
        sf.close();

        SkillFile sfExpected = SkillFile.open("src/test/resources/age/age-example-delete-first.sf");
        SkillFile sfActual = SkillFile.open(path);
        compareSkillFiles(sfExpected, sfActual);
	}
	
	@Test
	void testDeleteAllObjects() throws Exception {
		Path path = tmpFile("age-example.delete.all.objects");

        SkillFile sf = SkillFile.open("src/test/resources/age/age-example.sf");
        sf.changePath(path);
        
        GarbageCollector.run(sf, null, false, true, true);

        sf.close();

        SkillFile sfExpected = SkillFile.open("src/test/resources/empty-file.sf");
        SkillFile sfActual = SkillFile.open(path);
        compareSkillFiles(sfExpected, sfActual);
        
        // empty SkillFile expected, therefore binary equality possible
        // empty SkillFile is 2 Bytes zeros
        Assertions.assertEquals(sha256(path), sha256(new File("src/test/resources/empty-file.sf").toPath()));
	}
	
	@Test
	void testDoubleRun() throws Exception {
		Path path = tmpFile("age-example.double.run");

        SkillFile sf = SkillFile.open("src/test/resources/age/age-example.sf");
        sf.changePath(path);
        
        Set<CollectionRoot> roots = new HashSet<>();
        roots.add(new CollectionRoot("age",0));
        
        GarbageCollector.run(sf, roots, false, true, true);
        GarbageCollector.run(sf, roots, false, true, true);
        
        sf.close();

        SkillFile sfExpected = SkillFile.open("src/test/resources/age/age-example.sf");
        SkillFile sfActual = SkillFile.open(path);
        compareSkillFiles(sfExpected, sfActual);
	}

}
