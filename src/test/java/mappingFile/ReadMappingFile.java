package mappingFile;

import java.io.FileNotFoundException;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import common.CommonSpecificationMappingTest;
import de.ust.skill.manipulator.specificationMapping.MappingfileParser.MappingFileParser;
import de.ust.skill.manipulator.specificationMapping.MappingfileParser.ParseException;
import de.ust.skill.manipulator.specificationMapping.MappingfileParser.TypeMapping;

class ReadMappingFile extends CommonSpecificationMappingTest {

	protected ReadMappingFile() {
		super("src/test/resources/specificationMapper/mappingFiles/", "../typesystem/specification.sf");
	}

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
	
	@Test
	void testReadUnicode() throws ParseException, FileNotFoundException {
		String file = "src/test/resources/specificationMapper/mappingFiles/unicodeChars.map";
		
		Map<String, TypeMapping> typeMappings = MappingFileParser.parseFile(file);
		
		TypeMapping t1 = typeMappings.get("∀");
		Assertions.assertEquals("∀", t1.getTypename());
		Assertions.assertNull(t1.getNewTypename());
		Assertions.assertEquals("π", t1.getFieldMapping("∃"));
		Assertions.assertEquals("λ", t1.getFieldMapping("∀"));
	
		TypeMapping t2 = typeMappings.get("∃");
		Assertions.assertEquals("∃", t2.getTypename());
		Assertions.assertNull(t2.getNewTypename());
		Assertions.assertEquals("☢", t2.getFieldMapping("∃"));
		Assertions.assertEquals("⇀", t2.getFieldMapping("∀"));
		
	}
}