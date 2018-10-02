package common;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.List;

import de.ust.skill.manipulator.CLI;
import de.ust.skill.manipulator.internal.SkillFile;

public abstract class CommonTest {
	protected static final boolean CLI_TEST = true;

	protected static Path tmpFile(String string) throws Exception {
		File r = File.createTempFile(string, ".sf");
		// TODO
		// r.deleteOnExit();
		return r.toPath();
	}

	protected final static String sha256(String name) throws Exception {
		return sha256(new File("src/test/resources/" + name).toPath());
	}

	protected final static String sha256(Path path) throws Exception {
		byte[] bytes = Files.readAllBytes(path);
		StringBuilder sb = new StringBuilder();
		for (byte b : MessageDigest.getInstance("SHA-256").digest(bytes))
			sb.append(String.format("%02X", b));
		return sb.toString();
	}

	protected static void executeCliTest(String filename, String expectedFilename, List<String> specialArgs)
			throws Exception {
		
		Path path = tmpFile(expectedFilename);

		specialArgs.add("-i");
		specialArgs.add(filename);
		specialArgs.add("-o");
		specialArgs.add(path.toString());

		CLI.main(specialArgs.toArray(new String[specialArgs.size()]));
		
		SkillFile sfExpected;
		if(Files.exists(Paths.get(expectedFilename))) sfExpected = SkillFile.open(expectedFilename);
		else sfExpected = SkillFile.open("src/test/resources/empty-file.sf");
			
		SkillFile sfActual = SkillFile.open(path);
		SkillfileComparator.compareSkillFiles(sfExpected, sfActual);
	}

}
