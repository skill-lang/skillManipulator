package restrictions;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import common.CommonSpecificationMappingTest;
import common.SkillfileComparator;
import de.ust.skill.manipulator.internal.SkillFile;
import de.ust.skill.manipulator.internal.SkillState;

class RestrictionsAll extends CommonSpecificationMappingTest {

	protected RestrictionsAll() {
		super("src/test/resources/restrictions/", "restrictionsAll.sf");
	}

	@Test
	void testInOut() throws Exception {
		Path path = tmpFile("restrictions.in.out");

        SkillFile sf = SkillFile.open("src/test/resources/restrictions/restrictionsAll.sf");
        sf.changePath(path);
        ((SkillState)sf).prettyPrint();
        sf.close();
        
        SkillFile sfActual = SkillFile.open(path);
        SkillFile sfExpected = SkillFile.open("src/test/resources/restrictions/restrictionsAll.sf");
        SkillfileComparator.compareSkillFiles(sfExpected, sfActual);
	}
}
