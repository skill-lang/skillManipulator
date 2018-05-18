package gcPerformance;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import common.CommonPerformanceTest;

class CustomDirectoryPerformanceTest extends CommonPerformanceTest{
	public CustomDirectoryPerformanceTest() {
		super(EXECUTIONS, EXPECTED_FOLDER);
	}


	private static final String TESTFOLDER = "/media/olibroe/DATA/Ubuntu_Data/imlfiles/";
	private static final String EXPECTED_FOLDER = null;
	private static final int EXECUTIONS = 10;

	
	@TestFactory
    Stream<DynamicTest> dynamicTestsFromStream() throws Exception {
		return Files.walk(Paths.get(TESTFOLDER), 1)
				.filter(path -> path.toString().endsWith(".sf"))
				.map(path -> dynamicTest("test_" + path.getFileName().toString(), () -> {
					executeGC(path);
				}));
    }
}
