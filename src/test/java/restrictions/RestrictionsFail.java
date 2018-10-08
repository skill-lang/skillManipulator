package restrictions;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.Assertions;

import common.CommonSpecificationMappingTest;
import de.ust.skill.common.java.api.SkillException;
import de.ust.skill.ir.TypeContext;
import de.ust.skill.manipulator.internal.SkillFile;
import de.ust.skill.manipulator.specificationMapping.SpecificationMapper;
import de.ust.skill.parser.Parser;

class RestrictionsFail extends CommonSpecificationMappingTest {

	private static final String FOLDER = "src/test/resources/restrictionsFail/";

	/**
	 * Define test cases in constructor.
	 */
	protected RestrictionsFail() {
		super(FOLDER, FOLDER + "../restrictions/restrictionsAll.sf");
	}
	
	/**
	 * Overwrite test method of parent. This is because in the defined mappings a exception will be thrown.
	 * Exception is the result of failing restriction checks.
	 */
	protected void executeMapping(Path srcPath) throws Exception {
		String src = srcPath.toString();
		Path tmpfile = tmpFile("restriction.exception");
		
		Assertions.assertThrows(SkillException.class, () -> {
			SkillFile sf = SkillFile.open(startfile);
			TypeContext tc = Parser.process(new File(src), false, false, false, false);
			tc = tc.removeSpecialDeclarations();
			SpecificationMapper mapper = new SpecificationMapper();
			mapper.map(tc, sf, tmpfile);
		});
	}
}
