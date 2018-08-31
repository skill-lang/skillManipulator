package fieldRemoval;

import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import common.CommonTest;
import de.ust.skill.skillManipulator.internal.SkillFile;
import de.ust.skill.skillManipulator.utils.FieldUtils;

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
	
	@Test
	void testRemoveSlocOfImlroot() throws Exception {
		Path path = tmpFile("simple.iml.remove.sloc");

        SkillFile sf = SkillFile.open("src/test/resources/simpleIml/simple.iml.sf");
        sf.changePath(path);
        
        Assertions.assertTrue(FieldUtils.removeField(sf, "sloc", "imlroot"));
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/simpleIml/simpleIml-removed-sloc-of-imlroot.sf");
        compareSkillFiles(sfExpected, sf);
	}
	
	@Test
	void testRemoveNodeIdFail() throws Exception {
		Path path = tmpFile("simple.iml.remove.nodeid");

        SkillFile sf = SkillFile.open("src/test/resources/simpleIml/simple.iml.sf");
        sf.changePath(path);
        
        Assertions.assertFalse(FieldUtils.removeField(sf, "nodeid", "imlroot"));
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/simpleIml/simple.iml.sf");
        compareSkillFiles(sfExpected, sf);
	}

}
