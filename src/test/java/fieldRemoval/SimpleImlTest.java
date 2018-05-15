package fieldRemoval;

import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import common.CommonTest;
import de.ust.skill.skillManipulator.FieldUtils;
import de.ust.skill.skillManipulator.SkillFile;

class SimpleImlTest extends CommonTest{

	@Test
	void testRemoveNameOfIdentifier() throws Exception {
		Path path = tmpFile("simple.iml.remove.name");

        SkillFile sf = SkillFile.open("src/test/resources/simpleIml/simple.iml.sf");
        sf.changePath(path);
        
        Assertions.assertTrue(FieldUtils.removeField(sf, "name", "identifier"));
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/simpleIml/simpleIml-removed-name-of-identifier.sf");
        compareSkillFiles(sfExpected, sf);
	}
	
	@Test
	void testRemovePathnameOfSloc() throws Exception {
		Path path = tmpFile("simple.iml.remove.pathname");

        SkillFile sf = SkillFile.open("src/test/resources/simpleIml/simple.iml.sf");
        sf.changePath(path);
        
        Assertions.assertTrue(FieldUtils.removeField(sf, "pathname", "sloc"));
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/simpleIml/simpleIml-removed-pathname-of-sloc.sf");
        compareSkillFiles(sfExpected, sf);
	}

}
