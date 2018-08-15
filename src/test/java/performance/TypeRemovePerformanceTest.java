package performance;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import common.CommonPerformanceTest;
import de.ust.skill.skillManipulator.SkillFile;
import de.ust.skill.skillManipulator.TypeUtils;

class TypeRemovePerformanceTest extends CommonPerformanceTest{
	private static final int EXECUTIONS = 10;
	private static final String TESTFOLDER = "/media/olibroe/DATA/Ubuntu_Data/imlfiles2/";
	
	private String typename;
	
	public TypeRemovePerformanceTest() {
		super(EXECUTIONS);
	}

	@Disabled
	@TestFactory
    Stream<DynamicTest> testRemoveValue() throws Exception {
		typename = "value";
		return Files.walk(Paths.get(TESTFOLDER), 1)
				.filter(path -> path.toString().endsWith(".sf"))
				.map(path -> dynamicTest("test_" + path.getFileName().toString(), () -> {
					startExecution(path);
				}));
    }
	
	@Disabled
	@TestFactory
    Stream<DynamicTest> testRemoveSloc() throws Exception {
		typename = "sloc";
		return Files.walk(Paths.get(TESTFOLDER), 1)
				.filter(path -> path.toString().endsWith(".sf"))
				.map(path -> dynamicTest("test_" + path.getFileName().toString(), () -> {
					startExecution(path);
				}));
    }

	@Override
	protected void executeMethod(SkillFile sf) {
		TypeUtils.deleteType(sf, typename);
	}

}
