package gcTest;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import common.CommonTest;
import de.ust.skill.skillManipulator.GarbageCollector;
import de.ust.skill.skillManipulator.SkillFile;
import de.ust.skill.skillManipulator.GarbageCollector.CollectionRoot;

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
        
        Assert.assertTrue(sha256(path).equals(sha256(new File("src/test/resources/age/age-example.sf").toPath())));
	}
	
	@Test
	void testDeleteOneObject() throws Exception {
		Path path = tmpFile("age-example.delete.one");

        SkillFile sf = SkillFile.open("src/test/resources/age/age-example.sf");
        sf.changePath(path);
        
        Set<CollectionRoot> roots = new HashSet<>();
        roots.add(new CollectionRoot("age",1));

        GarbageCollector.run(sf, roots, false, true, true);
        
        sf.close();

        Assert.assertTrue(sha256(path).equals(sha256(new File("src/test/resources/age/age-example-delete-one.sf").toPath())));
	}
	
	@Test
	void testDeleteAllObjects() throws Exception {
		Path path = tmpFile("age-example.delete.all.objects");

        SkillFile sf = SkillFile.open("src/test/resources/age/age-example.sf");
        sf.changePath(path);
        
        GarbageCollector.run(sf, null, false, true, true);

        sf.close();

        Assert.assertTrue(sha256(path).equals(sha256(new File("src/test/resources/empty-file.sf").toPath())));
	}
	
	@Test
	void testDoubleRun() throws Exception {
		Path path = tmpFile("age-example.double.run");

        SkillFile sf = SkillFile.open("src/test/resources/age/age-example.sf");
        sf.changePath(path);
        
        Set<CollectionRoot> roots = new HashSet<>();
        roots.add(new CollectionRoot("age",1));
        
        GarbageCollector.run(sf, roots, false, true, true);
        GarbageCollector.run(sf, roots, false, true, true);
        
        sf.close();

        Assert.assertTrue(sha256(path).equals(sha256(new File("src/test/resources/age/age-example-delete-one.sf").toPath())));
	}

}
