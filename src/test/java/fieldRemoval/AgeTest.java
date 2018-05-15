package fieldRemoval;

import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import common.CommonTest;
import de.ust.skill.skillManipulator.FieldUtils;
import de.ust.skill.skillManipulator.SkillFile;

class AgeTest extends CommonTest{

	@Test
	void testRemoveFieldAge() throws Exception {
		Path path = tmpFile("age-example.remove.field");

        SkillFile sf = SkillFile.open("src/test/resources/age/age-example.sf");
        sf.changePath(path);
        
        Assertions.assertTrue(FieldUtils.removeField(sf, "age", "age"));
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/age/age-example-without-field.sf");
        compareSkillFiles(sfExpected, sf);
	}
	
	@Test
	void testRemoveAllv64() throws Exception {
		Path path = tmpFile("age-example.remove.v64");

        SkillFile sf = SkillFile.open("src/test/resources/age/age-example.sf");
        sf.changePath(path);
        
        // v64 => 11
        FieldUtils.removeAllFieldsOfType(sf, 11);
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/age/age-example-without-field.sf");
        compareSkillFiles(sfExpected, sf);
	}

}
