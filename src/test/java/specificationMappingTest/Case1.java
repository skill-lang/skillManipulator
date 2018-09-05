package specificationMappingTest;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import common.CommonTest;
import de.ust.skill.ir.TypeContext;
import de.ust.skill.manipulator.internal.SkillFile;
import de.ust.skill.manipulator.specificationMapping.SpecificationMapper;
import de.ust.skill.parser.Parser;

class Case1 extends CommonTest{

	@Test
	void testCase1_1() throws Exception {
		Path path = tmpFile("case1_1");

        SkillFile sf = SkillFile.open("src/test/resources/specificationMapper/case1/skillfile.sf");

        TypeContext tc = Parser.process(new File("src/test/resources/specificationMapper/case1/newSpecification.skill"), false, false, false, false);
		tc = tc.removeSpecialDeclarations();
        
		SkillFile newSf = SpecificationMapper.map(tc, sf, path);
        newSf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/specificationMapper/case1/result.sf");
        compareSkillFiles(sfExpected, SkillFile.open(path.toString()));
	}

	
	@Test
	void testCase1_2() throws Exception {
		Path path = tmpFile("case1_2");

        SkillFile sf = SkillFile.open("src/test/resources/specificationMapper/case1/skillfile.sf");

        TypeContext tc = Parser.process(new File("src/test/resources/specificationMapper/case1/newSpecification2.skill"), false, false, false, false);
		tc = tc.removeSpecialDeclarations();
        
		SkillFile newSf = SpecificationMapper.map(tc, sf, path);
        newSf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/specificationMapper/case1/result2.sf");
        compareSkillFiles(sfExpected, SkillFile.open(path.toString()));
	}
	
	@Test
	void testCase1_3() throws Exception {
		Path path = tmpFile("case1_3");

        SkillFile sf = SkillFile.open("src/test/resources/specificationMapper/case1/skillfile.sf");

        TypeContext tc = Parser.process(new File("src/test/resources/specificationMapper/case1/newSpecification3.skill"), false, false, false, false);
		tc = tc.removeSpecialDeclarations();
        
		SkillFile newSf = SpecificationMapper.map(tc, sf, path);
        newSf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/specificationMapper/case1/result3.sf");
        compareSkillFiles(sfExpected, SkillFile.open(path.toString()));
	}
	

}
