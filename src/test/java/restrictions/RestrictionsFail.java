package restrictions;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import common.CommonSpecificationMappingTest;

class RestrictionsFail extends CommonSpecificationMappingTest {

	private static final String FOLDER = "src/test/resources/restrictionsFail/";

	protected RestrictionsFail() {
		super(FOLDER, FOLDER + "../restrictions/restrictionsAll.sf");
	}
	
	protected void executeMapping(Path srcPath) throws Exception {
		String src = srcPath.toString();
		String expected = "src/test/resources/empty-file.sf";
		String map = src.replaceAll(".skill", ".map");
		
		List<String> args = new ArrayList<>();
		args.add("-specmap");
		
		args.add("-spec"); args.add(src);
		
		if(Files.exists(Paths.get(map))) {
			args.add("-map");
			args.add(map);
		}
		
        executeCliTest(startfile, expected, args);
	}
}
