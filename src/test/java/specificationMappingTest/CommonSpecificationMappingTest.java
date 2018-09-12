package specificationMappingTest;

import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import common.CommonTest;
import common.SkillfileComparator;
import de.ust.skill.ir.TypeContext;
import de.ust.skill.manipulator.internal.SkillFile;
import de.ust.skill.manipulator.specificationMapping.SpecificationMapper;
import de.ust.skill.parser.Parser;

public abstract class CommonSpecificationMappingTest extends CommonTest {
	private final String sourceFolder;
	private final String startfile;
	
	protected CommonSpecificationMappingTest(String sourceFolder, String startfile) {
		this.sourceFolder = sourceFolder;
		this.startfile = startfile;
	}
	
	@TestFactory
    Stream<DynamicTest> dynamicTests() throws Exception {
		return Files.walk(Paths.get(sourceFolder), 1)
				.filter(path -> path.getFileName().toString().endsWith(".skill"))
				.map(path -> dynamicTest("test_" + path.getFileName().toString(), () -> {
					executeMapping(path);
				}));
    }
	
	
	private void executeMapping(Path srcPath) throws Exception {
		String src = srcPath.toString();
		String expected = src.replaceAll(".skill", ".sf");

		Path path = tmpFile(src);

        SkillFile sf = SkillFile.open(sourceFolder + startfile);

        TypeContext tc = Parser.process(new File(srcPath.toString()), false, false, false, false);
		tc = tc.removeSpecialDeclarations();
        
		SkillFile newSf = SpecificationMapper.map(tc, sf, path);
        newSf.close();
        
        SkillFile sfExpected = SkillFile.open(expected);
        SkillfileComparator.compareSkillFiles(sfExpected, SkillFile.open(path.toString()));
	}
}
