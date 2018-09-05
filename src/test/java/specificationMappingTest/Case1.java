package specificationMappingTest;

import java.io.File;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import common.CommonTest;
import de.ust.skill.ir.TypeContext;
import de.ust.skill.parser.Parser;
import de.ust.skill.skillManipulator.internal.SkillFile;
import specificationMapping.SpecificationMapper;

class ProjectToSupertype extends CommonTest{

	@Test
	void testCase1() throws Exception {
		Path path = tmpFile("project.to.supertype");

        SkillFile sf = SkillFile.open("src/test/resources/specificationMapper/case1/skillfile.sf");

        TypeContext tc = Parser.process(new File("src/test/resources/specificationMapper/case1/newSpecification2.skill"), false, false, false, false);
		tc = tc.removeSpecialDeclarations();
        
		SkillFile newSf = SpecificationMapper.map(tc, sf, path);
        newSf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/specificationMapper/case1/result2.sf");
        compareSkillFiles(sfExpected, SkillFile.open(path.toString()));
	}
	
	@Test
	void testCase2Part1() throws Exception {
		Path path = tmpFile("project.to.supertype");

        SkillFile sf = SkillFile.open("src/test/resources/specificationMapper/case2/skillfile.sf");

        TypeContext tc = Parser.process(new File("src/test/resources/specificationMapper/case2/newSpecification2.skill"), false, false, false, false);
		tc = tc.removeSpecialDeclarations();
        
		SkillFile newSf = SpecificationMapper.map(tc, sf, path);
        newSf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/specificationMapper/case2/result2.sf");
        compareSkillFiles(sfExpected, SkillFile.open(path.toString()));
	}
	
	@Test
	void testCase2Part2() throws Exception {
		Path path = tmpFile("project.to.supertype");

        SkillFile sf = SkillFile.open("src/test/resources/specificationMapper/case2/skillfile.sf");

        TypeContext tc = Parser.process(new File("src/test/resources/specificationMapper/case2/newSpecification3.skill"), false, false, false, false);
		tc = tc.removeSpecialDeclarations();
        
		SkillFile newSf = SpecificationMapper.map(tc, sf, path);
        newSf.close();
        
        SkillFile sfExpected = SkillFile.open("src/test/resources/specificationMapper/case2/result3.sf");
        compareSkillFiles(sfExpected, SkillFile.open(path.toString()));
	}

}
