package typeRemoval;

import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import common.CommonTest;
import de.ust.skill.skillManipulator.SkillFile;
import de.ust.skill.skillManipulator.TypeUtils;

class SimpleImlTest extends CommonTest{

	@Test
	void testRemoveAattributable() throws Exception {
		Path path = tmpFile("simple.iml.remove.attributable");

        SkillFile sf = SkillFile.open("src/test/resources/simpleIml/simple.iml.sf");
        sf.changePath(path);
        
        Assertions.assertTrue(TypeUtils.deleteType(sf, "attributable"));
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/simpleIml/simpleIml-removed-attributable.sf");
        compareSkillFiles(sfExpected, sf);
	}
	
	@Test
	void testRemoveSloc() throws Exception {
		Path path = tmpFile("simple.iml.remove.sloc");

        SkillFile sf = SkillFile.open("src/test/resources/simpleIml/simple.iml.sf");
        sf.changePath(path);
        
        Assertions.assertTrue(TypeUtils.deleteType(sf, "sloc"));
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/simpleIml/simpleIml-removed-sloc.sf");
        compareSkillFiles(sfExpected, sf);
	}
	
	@Test
	void testRemoveIdentifier() throws Exception {
		Path path = tmpFile("simple.iml.remove.identifier");

        SkillFile sf = SkillFile.open("src/test/resources/simpleIml/simple.iml.sf");
        sf.changePath(path);
        
        Assertions.assertTrue(TypeUtils.deleteType(sf, "identifier"));
        
        sf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/simpleIml/simpleIml-removed-identifier.sf");
        compareSkillFiles(sfExpected, sf);
	}


}
