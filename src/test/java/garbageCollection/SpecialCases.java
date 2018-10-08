package garbageCollection;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import common.CommonGcTest;
import common.SkillfileComparator;
import de.ust.skill.common.java.api.Access;
import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.manipulator.gc.CollectionRoot;
import de.ust.skill.manipulator.gc.GarbageCollector;
import de.ust.skill.manipulator.internal.SkillFile;

class SpecialCases extends CommonGcTest{
	private final static String folder = "src/test/resources/gcSpecialCases/";
	private final static String startFile = folder + "specification.sf";
	
	/**
	 * Define test cases in constructor.
	 */
	protected SpecialCases() {
		gcTestDefinitions.add(new GcTestDefinition("test_RootT", startFile,
				folder + "result.sf", "t"));
		gcTestDefinitions.add(new GcTestDefinition("test_RootT_keepColl", startFile,
				folder + "specification.sf", "t", true));
	}

	/**
	 * Root is deleted, then GC is executed.
	 * Empty result file is expected.
	 * 
	 * @throws Exception
	 */
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
        SkillfileComparator.compareSkillFiles(sfExpected, sfActual);
	}
	
	/**
	 * Test command line options.
	 * 
	 * @throws Exception
	 */
	@Test
	void testCliGcOptions_1() throws Exception {
		List<String> args = new ArrayList<>();
		args.add("-gc");

		args.add("-p");
		args.add("-d");

		String startFile = "src/test/resources/simpleIml/simple.iml.sf";
		String dryRunExpected = "src/test/resources/empty-file.sf";
		executeCliTest(startFile, dryRunExpected, args);
	}
	
	/**
	 * Test command line options.
	 * 
	 * @throws Exception
	 */
	@Test
	void testCliGcOptions_2() throws Exception {
		List<String> args = new ArrayList<>();
		args.add("-gc");

		args.add("-r"); args.add("imlgraph");
		args.add("-s");
		args.add("-d");

		String startFile = "src/test/resources/simpleIml/simple.iml.sf";
		String dryRunExpected = "src/test/resources/empty-file.sf";
		executeCliTest(startFile, dryRunExpected, args);
	}
}
