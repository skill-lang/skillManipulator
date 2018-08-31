package performance;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import common.CommonPerformanceTest;
import de.ust.skill.skillManipulator.internal.SkillFile;
import de.ust.skill.skillManipulator.utils.FieldUtils;

class FieldRemovePerformanceTest extends CommonPerformanceTest{
	private static final int EXECUTIONS = 10;
	private static final String TESTFOLDER = "/media/olibroe/DATA/Ubuntu_Data/imlfiles/";
	
	private String typename;
	private String fieldname;
	
	public FieldRemovePerformanceTest() {
		super(EXECUTIONS);
	}

	@Disabled
	@TestFactory
    Stream<DynamicTest> testRemoveIsExplicitFromOconstructor() throws Exception {
		typename = "oconstructor";
		fieldname = "isexplicit";
		return Files.walk(Paths.get(TESTFOLDER), 1)
				.filter(path -> path.toString().endsWith(".sf"))
				.map(path -> dynamicTest("test_" + path.getFileName().toString(), () -> {
					startExecution(path);
				}));
    }

	@Override
	protected void executeMethod(SkillFile sf) {
		FieldUtils.removeField(sf, fieldname, typename);
		
	}

}
