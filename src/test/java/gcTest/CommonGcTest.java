package gcTest;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import common.CommonTest;

public abstract class CommonGcTest extends CommonTest {
	protected class GcTestDefinition {
		private String testname;
		private String file;
		private String expectedFile;
		private String roots;
		private boolean keepCollections;
		
		GcTestDefinition(String testname, String file, String expectedFile, String roots) {
			this.testname = testname;
			this.file = file;
			this.expectedFile = expectedFile;
			this.roots = roots;
			this.keepCollections = false;
		}
		
		GcTestDefinition(String testname, String file, String expectedFile, String roots, boolean kC) {
			this.testname = testname;
			this.file = file;
			this.expectedFile = expectedFile;
			this.roots = roots;
			this.keepCollections = kC;
		}
	}
	
	protected List<GcTestDefinition> gcTestDefinitions = new ArrayList<>();
	
	@TestFactory
    Stream<DynamicTest> dynamicTests() throws Exception {
		return gcTestDefinitions.stream()
				.map(def -> dynamicTest(def.testname, () -> {
					List<String> args = new ArrayList<>();
					args.add("-gc");

					if (def.roots != null) {
						args.add("-r");
						args.add(def.roots);
					}

					if (def.keepCollections) args.add("-kC");

					executeCliTest(def.file, def.expectedFile, args);
				}));
    }

}
