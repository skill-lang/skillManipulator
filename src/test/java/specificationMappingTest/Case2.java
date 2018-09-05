package specificationMappingTest;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import common.CommonTest;
import de.ust.skill.ir.TypeContext;
import de.ust.skill.parser.Parser;
import de.ust.skill.skillManipulator.internal.SkillFile;
import specificationMapping.SpecificationMapper;

class RemoveSupertype extends CommonTest{

	@Test
	void testCase1() throws Exception {
		Path path = tmpFile("remove.abstract.supertype");

        SkillFile sf = SkillFile.open("src/test/resources/specificationMapper/case1/skillfile.sf");

        TypeContext tc = Parser.process(new File("src/test/resources/specificationMapper/case1/newSpecification.skill"), false, false, false, false);
		tc = tc.removeSpecialDeclarations();
        
		SkillFile newSf = SpecificationMapper.map(tc, sf, path);
        newSf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/specificationMapper/case1/result.sf");
        compareSkillFiles(sfExpected, SkillFile.open(path.toString()));
	}
	
	@Test
	void testCase2() throws Exception {
		Path path = tmpFile("remove.supertype");

        SkillFile sf = SkillFile.open("src/test/resources/specificationMapper/case2/skillfile.sf");

        TypeContext tc = Parser.process(new File("src/test/resources/specificationMapper/case2/newSpecification.skill"), false, false, false, false);
		tc = tc.removeSpecialDeclarations();
        
		SkillFile newSf = SpecificationMapper.map(tc, sf, path);
        newSf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/specificationMapper/case2/result.sf");
        compareSkillFiles(sfExpected, SkillFile.open(path.toString()));
	}

}
