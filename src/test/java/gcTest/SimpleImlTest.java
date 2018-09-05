package gcTest;

import java.io.File;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import common.CommonTest;
import de.ust.skill.common.java.api.Access;
import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.manipulator.gc.CollectionRoot;
import de.ust.skill.manipulator.gc.GarbageCollector;
import de.ust.skill.manipulator.internal.SkillFile;

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
        SkillFile sfActual = SkillFile.open(path);
        compareSkillFiles(sfExpected, sfActual);
	}
	
	@Test
	void testSimpleIml_keepCollections() throws Exception {
		Path path = tmpFile("simple.iml.keepCollections");

        SkillFile sf = SkillFile.open("src/test/resources/simpleIml/simple.iml.sf");
        sf.changePath(path);
        
        Set<CollectionRoot> roots = new HashSet<>();
        roots.add(new CollectionRoot("imlgraph"));
        
        GarbageCollector.run(sf, roots, true, true, true);
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/simpleIml/simple.iml.gc.keepCollection.sf");
        SkillFile sfActual = SkillFile.open(path);
        compareSkillFiles(sfExpected, sfActual);
	}

	@Test
	void testDoubleRun() throws Exception {
		Path path = tmpFile("simple.iml.double.run");

        SkillFile sf = SkillFile.open("src/test/resources/simpleIml/simple.iml.sf");
        sf.changePath(path);
        
        Set<CollectionRoot> roots = new HashSet<>();
        roots.add(new CollectionRoot("imlgraph",0));
        
        GarbageCollector.run(sf, roots, false, true, false);
        GarbageCollector.run(sf, roots, false, false, true);
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/simpleIml/simple.iml.gc.sf");
        SkillFile sfActual = SkillFile.open(path);
        compareSkillFiles(sfExpected, sfActual);
	}
	
	@Test
	void testRootPreviouslyDeleted() throws Exception {
		Path path = tmpFile("simple.root.deleted");

        SkillFile sf = SkillFile.open("src/test/resources/simpleIml/simple.iml.sf");
        sf.changePath(path);
        
        Set<CollectionRoot> roots = new HashSet<>();
        roots.add(new CollectionRoot("imlgraph"));
        
        for(Access<? extends SkillObject> t : sf.allTypes()) {
        	if(t.superName() == null) {
        		for(SkillObject o : t) {
        			sf.delete(o);
        		}
        	}
        }
        
        GarbageCollector.run(sf, roots, false, true, false);
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/empty-file.sf");
        SkillFile sfActual = SkillFile.open(path);
        compareSkillFiles(sfExpected, sfActual);
	}
	
	@Test
	void testEmpty() throws Exception {
		Path path = tmpFile("simple.iml.empty");

        SkillFile sf = SkillFile.open("src/test/resources/simpleIml/simple.iml.sf");
        sf.changePath(path);
        
        GarbageCollector.run(sf, null, false, false, false);

        sf.close();

        SkillFile sfExpected = SkillFile.open("src/test/resources/empty-file.sf");
        SkillFile sfActual = SkillFile.open(path);
        compareSkillFiles(sfExpected, sfActual);
        
        // empty SkillFile expected, therefore binary equality possible
        // empty SkillFile is 2 Bytes zeros
        Assertions.assertEquals(sha256(path), sha256(new File("src/test/resources/empty-file.sf").toPath()));
	}
	
	@Test
	void testWrongUsage() throws Exception {
		Path path = tmpFile("simple.iml.wrong.usage");

        SkillFile sf = SkillFile.open("src/test/resources/simpleIml/simple.iml.sf");
        sf.changePath(path);
        
        Set<CollectionRoot> roots = new HashSet<>();
        // this type is not in the typesystem
        roots.add(new CollectionRoot("IM_NO_TYPE"));
        
        GarbageCollector.run(sf, roots, false, false, false);

        sf.close();

        SkillFile sfExpected = SkillFile.open("src/test/resources/empty-file.sf");
        SkillFile sfActual = SkillFile.open(path);
        compareSkillFiles(sfExpected, sfActual);
        
        // empty SkillFile expected, therefore binary equality possible
        // empty SkillFile is 2 Bytes zeros
        Assertions.assertEquals(sha256(path), sha256(new File("src/test/resources/empty-file.sf").toPath()));
	}
	
	@Test
	void testWrongUsage_2() throws Exception {
		Path path = tmpFile("simple.iml.wrong.usage");

        SkillFile sf = SkillFile.open("src/test/resources/simpleIml/simple.iml.sf");
        sf.changePath(path);
        
        Set<CollectionRoot> roots = new HashSet<>();
        // there is no object with id 400
        roots.add(new CollectionRoot("imlgraph",400));
        
        GarbageCollector.run(sf, roots, false, false, false);

        sf.close();

        SkillFile sfExpected = SkillFile.open("src/test/resources/empty-file.sf");
        SkillFile sfActual = SkillFile.open(path);
        compareSkillFiles(sfExpected, sfActual);
        
        // empty SkillFile expected, therefore binary equality possible
        // empty SkillFile is 2 Bytes zeros
        Assertions.assertEquals(sha256(path), sha256(new File("src/test/resources/empty-file.sf").toPath()));
	}
}
