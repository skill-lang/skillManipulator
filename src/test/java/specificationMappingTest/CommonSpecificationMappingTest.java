package specificationMappingTest;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import common.CommonTest;

public abstract class CommonSpecificationMappingTest extends CommonTest {
	private final String sourceFolder;
	private final String startfile;
	
	protected CommonSpecificationMappingTest(String sourceFolder, String startfile) {
		this.sourceFolder = sourceFolder;
		this.startfile = sourceFolder + startfile;
	}
	
	@TestFactory
    Stream<DynamicTest> dynamicTests() throws Exception {
		return Files.walk(Paths.get(sourceFolder), 1)
				.filter(path -> path.getFileName().toString().endsWith(".skill"))
				.map(path -> dynamicTest("test_" + path.getFileName().toString(), () -> {
					executeMapping(path);
				}));
    }
	
	
	private void executeMapping(Path srcPath) throws Exception {
		String src = srcPath.toString();
		String expected = src.replaceAll(".skill", ".sf");
		
		List<String> args = new ArrayList<>();
		args.add("-map");
		
		args.add("-spec"); args.add(src);
		
        executeCliTest(startfile, expected, args);
	}
}
