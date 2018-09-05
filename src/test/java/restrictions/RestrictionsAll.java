package restrictions;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import common.CommonTest;
import de.ust.skill.common.java.api.Access;
import de.ust.skill.common.java.api.SkillException;
import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.common.java.internal.StaticDataIterator;
import de.ust.skill.ir.TypeContext;
import de.ust.skill.manipulator.internal.SkillFile;
import de.ust.skill.manipulator.specificationMapping.SpecificationMapper;
import de.ust.skill.parser.Parser;

class RestrictionsAll extends CommonTest{

	@Test
	void testInOut() throws Exception {
		Path path = tmpFile("restrictions.in.out");

        SkillFile sf = SkillFile.open("src/test/resources/restrictions/restrictionsAll.sf");
        sf.changePath(path);
        
        sf.close();
        
        SkillFile sfActual = SkillFile.open(path);
        SkillFile sfExpected = SkillFile.open("src/test/resources/restrictions/restrictionsAll.sf");
        compareSkillFiles(sfExpected, sfActual);
	}
	
	@Test
	void violateMonotone() throws Exception {
		Path path = tmpFile("violate.monotone");

        SkillFile sf = SkillFile.open("src/test/resources/restrictions/restrictionsAll.sf");
        sf.changePath(path);
        for(Access<? extends SkillObject> t : sf.allTypes()) {
        	if(t.name().equals("term")) {
        		StaticDataIterator<? extends SkillObject> sit = t.staticInstances();
        		SkillObject o = sit.next();
        		sf.delete(o);
        		break;
        	}
        }
        
        Assertions.assertThrows(SkillException.class, () -> { sf.close(); });
	}
	
	@TestFactory
    Stream<DynamicTest> dynamicTestsFromStream() throws Exception {
		return Files.walk(Paths.get("src/test/resources/restrictions/"), 1)
				.filter(path -> path.getFileName().toString().endsWith(".skill"))
				.map(path -> dynamicTest("test_" + path.getFileName().toString(), () -> {
					executeMapping(path);
				}));
    }

	private void executeMapping(Path srcPath) throws Exception {
		String src = srcPath.toString();
		String expected = src.replaceAll(".skill", ".sf");

		Path path = tmpFile(src);

        SkillFile sf = SkillFile.open("src/test/resources/restrictions/restrictionsAll.sf");

        TypeContext tc = Parser.process(new File(srcPath.toString()), false, false, false, false);
		tc = tc.removeSpecialDeclarations();
        
        SkillFile newSf = SpecificationMapper.map(tc, sf, path);
        newSf.close();
        
        SkillFile sfExpected = SkillFile.open(expected);
        compareSkillFiles(sfExpected, SkillFile.open(path.toString()));
	}
	
	@TestFactory
    Stream<DynamicTest> dynamicTestsFromStreamFail() throws Exception {
		return Files.walk(Paths.get("src/test/resources/restrictionsFail/"), 1)
				.filter(path -> path.getFileName().toString().endsWith(".skill"))
				.map(path -> dynamicTest("test_" + path.getFileName().toString(), () -> {
					executeMappingFail(path);
				}));
    }
	

	private void executeMappingFail(Path srcPath) throws Exception {
		String src = srcPath.toString();

		Path path = tmpFile(src);

        SkillFile sf = SkillFile.open("src/test/resources/restrictions/restrictionsAll.sf");

        TypeContext tc = Parser.process(new File(srcPath.toString()), false, false, false, false);
		final TypeContext tcCleaned = tc.removeSpecialDeclarations();
 
        Assertions.assertThrows(SkillException.class, () -> { SpecificationMapper.map(tcCleaned, sf, path); });
	}

}
