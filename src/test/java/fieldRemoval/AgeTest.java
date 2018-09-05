package fieldRemoval;

import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import common.CommonTest;
import de.ust.skill.manipulator.internal.SkillFile;
import de.ust.skill.manipulator.utils.FieldUtils;

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
	void testRemoveOfNonExistingType() throws Exception {
		Path path = tmpFile("age-example.remove.field");

        SkillFile sf = SkillFile.open("src/test/resources/age/age-example.sf");
        sf.changePath(path);
        
        Assertions.assertFalse(FieldUtils.removeField(sf, "age", "TYPE_DOES_NOT_EXIST"));
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/age/age-example.sf");
        compareSkillFiles(sfExpected, sf);
	}

}
