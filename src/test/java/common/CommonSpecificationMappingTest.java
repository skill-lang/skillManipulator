package common;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

public abstract class CommonSpecificationMappingTest extends CommonTest {
	protected final String sourceFolder;
	protected final String startfile;
	
	protected CommonSpecificationMappingTest(String sourceFolder, String startfile) {
		this.sourceFolder = sourceFolder;
		this.startfile = sourceFolder + startfile;
	}
	
	@TestFactory
	protected Stream<DynamicTest> dynamicTests() throws Exception {
		return Files.walk(Paths.get(sourceFolder), 1)
				.filter(path -> path.getFileName().toString().endsWith(".skill"))
				.map(path -> dynamicTest("test_" + path.getFileName().toString(), () -> {
					executeMapping(path);
				}));
    }
	
	
	protected void executeMapping(Path srcPath) throws Exception {
		String src = srcPath.toString();
		String expected = src.replaceAll(".skill", ".sf");
		String map = src.replaceAll(".skill", ".map");
		
		List<String> args = new ArrayList<>();
		args.add("-specmap");
		
		args.add("-spec"); args.add(src);
		
		if(Files.exists(Paths.get(map))) {
			args.add("-map");
			args.add(map);
		}
		
        executeCliTest(startfile, expected, args);
	}
}
