package mappingFileTest;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import common.CommonTest;
import de.ust.skill.ir.TypeContext;
import de.ust.skill.manipulator.internal.SkillFile;
import de.ust.skill.manipulator.specificationMapping.SpecificationMapper;
import de.ust.skill.manipulator.specificationMapping.MappingfileParser.MappingFileParser;
import de.ust.skill.manipulator.specificationMapping.MappingfileParser.ParseException;
import de.ust.skill.manipulator.specificationMapping.MappingfileParser.TypeMapping;
import de.ust.skill.parser.Parser;

class ReadMappingFile extends CommonTest{

	@Test
	void testReadSimple() throws ParseException, FileNotFoundException {
		String file = "src/test/resources/specificationMapper/mappingFiles/simple.map";
		
		Map<String, TypeMapping> typeMappings = MappingFileParser.parseFile(file);
		
		TypeMapping t1 = typeMappings.get("test");
		Assertions.assertEquals("test", t1.getTypename());
		Assertions.assertEquals("test2", t1.getNewTypename());
		
		TypeMapping t2 = typeMappings.get("mytype");
		Assertions.assertEquals("mytype", t2.getTypename());
		Assertions.assertEquals("customtype", t2.getNewTypename());
		Assertions.assertEquals("field2", t2.getFieldMapping("field"));
		Assertions.assertEquals("g", t2.getFieldMapping("f"));
		
		TypeMapping t3 = typeMappings.get("anothertest");
		Assertions.assertEquals("anothertest", t3.getTypename());
		Assertions.assertNull(t3.getNewTypename());
		Assertions.assertEquals("gamma", t3.getFieldMapping("delta"));
	}

	@TestFactory
    Stream<DynamicTest> dynamicTestsFromStream() throws Exception {
		return Files.walk(Paths.get("src/test/resources/specificationMapper/mappingFiles/"), 1)
				.filter(path -> path.getFileName().toString().endsWith(".skill"))
				.map(path -> dynamicTest("test_" + path.getFileName().toString(), () -> {
					executeMapping(path);
				}));
    }

	private void executeMapping(Path srcPath) throws Exception {
		String src = srcPath.toString();
		String expected = src.replaceAll(".skill", ".sf");
		String map = src.replaceAll(".skill", ".map");

		Path path = tmpFile(src);

        SkillFile sf = SkillFile.open("src/test/resources/specificationMapper/typesystem/specification.sf");

        TypeContext tc = Parser.process(new File(srcPath.toString()), false, false, false, false);
		tc = tc.removeSpecialDeclarations();
        
		SkillFile newSf = SpecificationMapper.map(tc, sf, path, map);
        newSf.close();
        
        SkillFile sfExpected = SkillFile.open(expected);
        compareSkillFiles(sfExpected, SkillFile.open(path.toString()));
	}
}
