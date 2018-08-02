package gcTest;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import common.CommonTest;
import de.ust.skill.skillManipulator.GarbageCollector;
import de.ust.skill.skillManipulator.GarbageCollector.CollectionRoot;
import de.ust.skill.skillManipulator.SkillFile;

class EmptyTypePoolTest extends CommonTest{

	@Test
	void testReference() throws Exception {
		Path path = tmpFile("reference");

        SkillFile sf = SkillFile.open("src/test/resources/EmptyTypePool/Reference/reference.sf");
        sf.changePath(path);
        
        Set<CollectionRoot> roots = new HashSet<>();
        roots.add(new CollectionRoot("t"));
        
        GarbageCollector.run(sf, roots, false, true, true);
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/EmptyTypePool/Reference/result.sf");
        SkillFile sfActual = SkillFile.open(path);
        compareSkillFiles(sfExpected, sfActual);
	}
	
	@Test
	void testReferenceKeepCollection() throws Exception {
		Path path = tmpFile("reference.keep");

        SkillFile sf = SkillFile.open("src/test/resources/EmptyTypePool/Reference/reference.sf");
        sf.changePath(path);
        
        Set<CollectionRoot> roots = new HashSet<>();
        roots.add(new CollectionRoot("t"));
        
        GarbageCollector.run(sf, roots, true, true, true);
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/EmptyTypePool/Reference/result.sf");
        SkillFile sfActual = SkillFile.open(path);
        compareSkillFiles(sfExpected, sfActual);
	}
	
	@Test
	void testArray() throws Exception {
		Path path = tmpFile("array");

        SkillFile sf = SkillFile.open("src/test/resources/EmptyTypePool/Array/array.sf");
        sf.changePath(path);
        
        Set<CollectionRoot> roots = new HashSet<>();
        roots.add(new CollectionRoot("t"));
        
        GarbageCollector.run(sf, roots, false, true, true);
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/EmptyTypePool/Array/result.sf");
        SkillFile sfActual = SkillFile.open(path);
        compareSkillFiles(sfExpected, sfActual);
	}
	
	@Test
	void testMap() throws Exception {
		Path path = tmpFile("map");

        SkillFile sf = SkillFile.open("src/test/resources/EmptyTypePool/Map/map.sf");
        sf.changePath(path);
        
        Set<CollectionRoot> roots = new HashSet<>();
        roots.add(new CollectionRoot("t"));
        
        GarbageCollector.run(sf, roots, false, true, true);
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/EmptyTypePool/Map/result.sf");
        SkillFile sfActual = SkillFile.open(path);
        compareSkillFiles(sfExpected, sfActual);
	}
	
	@Test
	void testArrayKeepCollection() throws Exception {
		Path path = tmpFile("array.keep");

        SkillFile sf = SkillFile.open("src/test/resources/EmptyTypePool/Array/array.sf");
        sf.changePath(path);
        
        Set<CollectionRoot> roots = new HashSet<>();
        roots.add(new CollectionRoot("t"));
        
        GarbageCollector.run(sf, roots, true, true, true);
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/EmptyTypePool/Array/array.sf");
        SkillFile sfActual = SkillFile.open(path);
        compareSkillFiles(sfExpected, sfActual);
	}
	
	@Test
	void testMapKeepCollection() throws Exception {
		Path path = tmpFile("map.keep");

        SkillFile sf = SkillFile.open("src/test/resources/EmptyTypePool/Map/map.sf");
        sf.changePath(path);
        
        Set<CollectionRoot> roots = new HashSet<>();
        roots.add(new CollectionRoot("t"));
        
        GarbageCollector.run(sf, roots, true, true, true);
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/EmptyTypePool/Map/map.sf");
        SkillFile sfActual = SkillFile.open(path);
        compareSkillFiles(sfExpected, sfActual);
	}
	
	@Test
	void testReferenceVariation() throws Exception {
		Path path = tmpFile("reference.variation");

        SkillFile sf = SkillFile.open("src/test/resources/EmptyTypePool/Reference/reference_variation.sf");
        sf.changePath(path);
        
        Set<CollectionRoot> roots = new HashSet<>();
        roots.add(new CollectionRoot("u"));
        
        GarbageCollector.run(sf, roots, false, true, true);
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/EmptyTypePool/Reference/result_variation.sf");
        SkillFile sfActual = SkillFile.open(path);
        compareSkillFiles(sfExpected, sfActual);
	}

}
