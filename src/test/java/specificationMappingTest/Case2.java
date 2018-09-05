package specificationMappingTest;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import common.CommonTest;
import de.ust.skill.ir.TypeContext;
import de.ust.skill.manipulator.internal.SkillFile;
import de.ust.skill.manipulator.specificationMapping.SpecificationMapper;
import de.ust.skill.parser.Parser;

class Case2 extends CommonTest{
	
	@Test
	void testCase2_1() throws Exception {
		Path path = tmpFile("case2_1");

        SkillFile sf = SkillFile.open("src/test/resources/specificationMapper/case2/skillfile.sf");

        TypeContext tc = Parser.process(new File("src/test/resources/specificationMapper/case2/newSpecification.skill"), false, false, false, false);
		tc = tc.removeSpecialDeclarations();
        
		SkillFile newSf = SpecificationMapper.map(tc, sf, path);
        newSf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/specificationMapper/case2/result.sf");
        compareSkillFiles(sfExpected, SkillFile.open(path.toString()));
	}

	@Test
	void testCase2_2() throws Exception {
		Path path = tmpFile("case2_2");

        SkillFile sf = SkillFile.open("src/test/resources/specificationMapper/case2/skillfile.sf");

        TypeContext tc = Parser.process(new File("src/test/resources/specificationMapper/case2/newSpecification2.skill"), false, false, false, false);
		tc = tc.removeSpecialDeclarations();
        
		SkillFile newSf = SpecificationMapper.map(tc, sf, path);
        newSf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/specificationMapper/case2/result2.sf");
        compareSkillFiles(sfExpected, SkillFile.open(path.toString()));
	}
	
	@Test
	void testCase2_3() throws Exception {
		Path path = tmpFile("case2_3");

        SkillFile sf = SkillFile.open("src/test/resources/specificationMapper/case2/skillfile.sf");

        TypeContext tc = Parser.process(new File("src/test/resources/specificationMapper/case2/newSpecification3.skill"), false, false, false, false);
		tc = tc.removeSpecialDeclarations();
        
		SkillFile newSf = SpecificationMapper.map(tc, sf, path);
        newSf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/specificationMapper/case2/result3.sf");
        compareSkillFiles(sfExpected, SkillFile.open(path.toString()));
	}
	
	@Test
	void testCase2_4() throws Exception {
		Path path = tmpFile("case2_4");

        SkillFile sf = SkillFile.open("src/test/resources/specificationMapper/case2/result2.sf");

        TypeContext tc = Parser.process(new File("src/test/resources/specificationMapper/case2/specification.skill"), false, false, false, false);
		tc = tc.removeSpecialDeclarations();
        
		SkillFile newSf = SpecificationMapper.map(tc, sf, path);
        newSf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/specificationMapper/case2/skillfile.sf");
        compareSkillFiles(sfExpected, SkillFile.open(path.toString()));
	}
}
