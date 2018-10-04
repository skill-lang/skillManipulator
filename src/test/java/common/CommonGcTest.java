package common;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Common test implementation for all GC-tests.
 * 
 * @author olibroe
 *
 */
public abstract class CommonGcTest extends CommonTest {
	/**
	 * Define a test case with this class.
	 * 
	 * @author olibroe
	 *
	 */
	protected class GcTestDefinition {
		private String testname;
		private String file;
		private String expectedFile;
		private String roots;
		private boolean keepCollections;
		
		public GcTestDefinition(String testname, String file, String expectedFile, String roots) {
			this.testname = testname;
			this.file = file;
			this.expectedFile = expectedFile;
			this.roots = roots;
			this.keepCollections = false;
		}
		
		public GcTestDefinition(String testname, String file, String expectedFile, String roots, boolean kC) {
			this.testname = testname;
			this.file = file;
			this.expectedFile = expectedFile;
			this.roots = roots;
			this.keepCollections = kC;
		}
	}
	
	protected List<GcTestDefinition> gcTestDefinitions = new ArrayList<>();
	
	/**
	 * Execute defined test cases.
	 * 
	 * @return
	 * @throws Exception
	 */
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
