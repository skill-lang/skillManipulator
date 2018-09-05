package typeRemoval;

import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import common.CommonTest;
import de.ust.skill.manipulator.internal.SkillFile;
import de.ust.skill.manipulator.utils.TypeUtils;

class AgeTest extends CommonTest{

	@Test
	void testRemoveAge() throws Exception {
		Path path = tmpFile("age-example.remove.type");

        SkillFile sf = SkillFile.open("src/test/resources/age/age-example.sf");
        sf.changePath(path);
        
        Assertions.assertTrue(TypeUtils.deleteType(sf, "age"));
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/empty-file.sf");
        compareSkillFiles(sfExpected, sf);
	}
	
	@Test
	void testRemoveTypeFails() throws Exception {
		Path path = tmpFile("age-example.remove.fail");

        SkillFile sf = SkillFile.open("src/test/resources/age/age-example.sf");
        sf.changePath(path);
        
        Assertions.assertFalse(TypeUtils.deleteType(sf, "norealtype"));
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/age/age-example.sf");
        compareSkillFiles(sfExpected, sf);
	}

}
