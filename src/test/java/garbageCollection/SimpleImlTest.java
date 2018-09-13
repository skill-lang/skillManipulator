package garbageCollection;

import common.CommonGcTest;

class SimpleImlTest extends CommonGcTest{
	private final static String folder = "src/test/resources/simpleIml/";
	private final static String startFile = folder + "simple.iml.sf";
	
	protected SimpleImlTest() {
		gcTestDefinitions.add(new GcTestDefinition("test_RootImlgraph", startFile,
				folder + "simple.iml.gc.sf", "imlgraph"));
		gcTestDefinitions.add(new GcTestDefinition("test_RootImlgraph_keepColl", startFile,
				folder + "simple.iml.gc.keepCollection.sf", "imlgraph#0", true));
		
		gcTestDefinitions.add(new GcTestDefinition("test_RootEmpty", startFile,
				folder + "../empty-file.sf", null));
		
		gcTestDefinitions.add(new GcTestDefinition("test_RootTypeNotFound", startFile,
				folder + "../empty-file.sf", "IM_NO_TYPE"));
		gcTestDefinitions.add(new GcTestDefinition("test_RootObjectNotFound", startFile,
				folder + "../empty-file.sf", "imlgraph#400"));
	}
}
