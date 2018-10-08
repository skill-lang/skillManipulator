package common;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import de.ust.skill.ir.TypeContext;
import de.ust.skill.manipulator.internal.SkillFile;
import de.ust.skill.manipulator.specificationMapping.SpecificationMapper;
import de.ust.skill.manipulator.specificationMapping.messages.FieldIncompatibleInformation;
import de.ust.skill.manipulator.specificationMapping.messages.MappingInformation;
import de.ust.skill.parser.Parser;

/**
 * Common implementation for all specification mapping tests.
 * 
 * @author olibroe
 *
 */
public abstract class CommonSpecificationMappingTest extends CommonTest {
	// tests for specification mapping are defined by a folder and a start file
	protected final String sourceFolder;
	protected final String startfile;
	
	protected CommonSpecificationMappingTest(String sourceFolder, String startfile) {
		this.sourceFolder = sourceFolder;
		this.startfile = startfile;
	}
	
	/**
	 * Search for all .skill files in the given folder and map the start file on them.
	 * The expected file has the same name as the .skill file, but with ending .sf.
	 * 
	 * @return
	 * @throws Exception
	 */
	@TestFactory
	protected Stream<DynamicTest> dynamicTests() throws Exception {
		return Files.walk(Paths.get(sourceFolder), 1)
				.filter(path -> path.getFileName().toString().endsWith(".skill"))
				.map(path -> dynamicTest("test_" + path.getFileName().toString(), () -> {
					executeMapping(path);
				}));
    }
	
	/**
	 * Start the mapping by giving the CLI the arguments.
	 * 
	 * @param srcPath
	 * @throws Exception
	 */
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
	
	/**
	 * For specifications that do not fit on the given skillfile, we have to make sure that the same fields are
	 * failing the test.
	 * 
	 * @param mapper
	 * @param expectedFailFields
	 */
	protected void compareFailingFields(SpecificationMapper mapper, Set<String> expectedFailFields) {
		Set<String> actualFailFields = new HashSet<>();
		Set<MappingInformation> mappingLog = mapper.getMappingLog();
		for(MappingInformation info : mappingLog) {
			if(info instanceof FieldIncompatibleInformation) {
				FieldIncompatibleInformation finfo = (FieldIncompatibleInformation) info;
				actualFailFields.add(finfo.oldPool.name() + "." + finfo.oldField.name() + "->" + 
						finfo.newPool.name() + "." + finfo.newField.name());
			}
		}
		Assertions.assertIterableEquals(expectedFailFields, actualFailFields);
	}
	
	/**
	 * Mapping execution for the expected failing tests.
	 * Returns the created specification mapper to execute the compareFailingFields(...)-method.
	 * 
	 * @param startFile
	 * @param specification
	 * @return
	 * @throws Exception
	 */
	protected SpecificationMapper executeMapping(String startFile, String specification) 
			throws Exception {
		Path path = tmpFile(specification);
		
		SkillFile sf = SkillFile.open(startfile);

		TypeContext tc = Parser.process(new File(specification), false, false, false, false);
		tc = tc.removeSpecialDeclarations();
		
		SpecificationMapper mapper = new SpecificationMapper();
		SkillFile newSf = mapper.map(tc, sf, path);
		
		Assertions.assertNull(newSf);
		return mapper;
	}
}
