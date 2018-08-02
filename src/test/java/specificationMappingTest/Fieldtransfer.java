package specificationMappingTest;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import common.CommonTest;
import de.ust.skill.ir.TypeContext;
import de.ust.skill.parser.Parser;
import de.ust.skill.skillManipulator.SkillFile;
import de.ust.skill.skillManipulator.SpecificationMapper;

class Fieldtransfer extends CommonTest{

	@TestFactory
    Stream<DynamicTest> dynamicTestsFromStream() throws Exception {
		return Files.walk(Paths.get("src/test/resources/specificationMapper/Fieldtransfer/"), 1)
				.filter(path -> path.getFileName().toString().endsWith(".skill"))
				.map(path -> dynamicTest("test_" + path.getFileName().toString(), () -> {
					executeMapping(path);
				}));
    }

	private void executeMapping(Path srcPath) throws Exception {
		String src = srcPath.toString();
		String expected = src.replaceAll(".skill", ".sf");

		Path path = tmpFile(src);

        SkillFile sf = SkillFile.open("src/test/resources/specificationMapper/Fieldtransfer/specification.sf");

        TypeContext tc = Parser.process(new File(srcPath.toString()), false, false, false, false);
		tc = tc.removeSpecialDeclarations();
        
        SpecificationMapper.map(tc, sf, path);
        
        SkillFile sfExpected = SkillFile.open(expected);
        compareSkillFiles(sfExpected, SkillFile.open(path.toString()));
	}

}
