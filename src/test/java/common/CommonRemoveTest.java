package common;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/**
 * Common test implementation for all type and field remove tests.
 * 
 * @author olibroe
 *
 */
public abstract class CommonRemoveTest extends CommonTest {
	protected abstract class RemoveTestDefinition {
		private String testname;
		private String file;
		private String expectedFile;
		private String typename;
		
		RemoveTestDefinition(String testname, String file, String expectedFile, String typename) {
			this.testname = testname;
			this.file = file;
			this.expectedFile = expectedFile;
			this.typename = typename;
		}
	}
	
	/**
	 * Define type remove test.
	 * 
	 * @author olibroe
	 *
	 */
	protected class RemoveTypeTestDefinition extends RemoveTestDefinition {
		public RemoveTypeTestDefinition(String testname, String file, String expectedFile, String typename) {
			super(testname, file, expectedFile, typename);
		}
		
	}
	
	/**
	 * Define field remove test.
	 * 
	 * @author olibroe
	 *
	 */
	protected class RemoveFieldTestDefinition extends RemoveTestDefinition {
		private String fieldname;
		
		public RemoveFieldTestDefinition(String testname, String file, String expectedFile,
				String typename, String fieldname) {
			super(testname, file, expectedFile, typename);
			this.fieldname = fieldname;
		}
	}
	
	protected List<RemoveTestDefinition> removeTestDefinitions = new ArrayList<>();
	
	/**
	 * Execute defined tests.
	 * 
	 * @return
	 * @throws Exception
	 */
	@TestFactory
    Stream<DynamicTest> dynamicTests() throws Exception {
		return removeTestDefinitions.stream()
				.map(def -> dynamicTest(def.testname, () -> {
					List<String> args = new ArrayList<>();
					args.add("-rm");

					if (def instanceof RemoveFieldTestDefinition) {
						args.add("-f");
						args.add(def.typename + "." + ((RemoveFieldTestDefinition) def).fieldname);
					} else {
						args.add("-t");
						args.add(def.typename);
					}

					executeCliTest(def.file, def.expectedFile, args);
				}));
    }
}
