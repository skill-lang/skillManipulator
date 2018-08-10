package performance;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import common.CommonPerformanceTest;
import de.ust.skill.skillManipulator.GarbageCollector;
import de.ust.skill.skillManipulator.SkillFile;
import de.ust.skill.skillManipulator.GarbageCollector.CollectionRoot;

/**
 * This is an automated Performance Test for the Garbage Collector.
 * Usage:
 * 	1. Set variable TESTFOLDER to a folder with the files you want to check performance on
 *  2. Remove disabled from the test
 *  3. Execute Test as JUnit Test
 * 
 * The test creates an .csv file in the output folder of the project
 * 
 * @author olibroe
 *
 */
class GcPerformanceTest extends CommonPerformanceTest{
	private static final String TESTFOLDER = "/media/olibroe/DATA/Ubuntu_Data/imlfiles/";
	private static final int EXECUTIONS = 10;
	
	private Set<CollectionRoot> roots = new HashSet<>();
	
	public GcPerformanceTest() {
		super(EXECUTIONS);
		roots.add(new CollectionRoot("imlgraph"));
	}

	@Disabled
	@TestFactory
    Stream<DynamicTest> gcPerformanceTest() throws Exception {
		return Files.walk(Paths.get(TESTFOLDER), 1)
				.filter(path -> path.toString().endsWith(".sf"))
				.map(path -> dynamicTest("test_" + path.getFileName().toString(), () -> {
					startExecution(path);
				}));
    }
	
	@Override
	protected void executeMethod(SkillFile sf) {
		GarbageCollector.run(sf, roots, false, false, false);
		
	}
	
	
}
