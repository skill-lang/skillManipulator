package restrictions;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import common.CommonSpecificationMappingTest;
import common.SkillfileComparator;
import de.ust.skill.manipulator.internal.SkillFile;
import de.ust.skill.manipulator.internal.SkillState;

class RestrictionsAll extends CommonSpecificationMappingTest {

	private static final String FOLDER = "src/test/resources/restrictions/";

	/**
	 * Define test cases in constructor.
	 */
	protected RestrictionsAll() {
		super(FOLDER, FOLDER + "restrictionsAll.sf");
	}

	/**
	 * This test checks the serialization and deserialization of all restrictions.
	 * 
	 * @throws Exception
	 */
	@Test
	void testInOut() throws Exception {
		Path path = tmpFile("restrictions.in.out");

        SkillFile sf = SkillFile.open("src/test/resources/restrictions/restrictionsAll.sf");
        sf.changePath(path);
        ((SkillState)sf).prettyPrint();
        sf.check();
        sf.close();
        
        SkillFile sfActual = SkillFile.open(path);
        SkillFile sfExpected = SkillFile.open("src/test/resources/restrictions/restrictionsAll.sf");
        SkillfileComparator.compareSkillFiles(sfExpected, sfActual);
	}
}
