package garbageCollection;

import common.CommonGcTest;

class AgeTest extends CommonGcTest{
	private final static String folder = "src/test/resources/age/";
	private final static String startFile = folder + "age-example.sf";
	
	protected AgeTest() {
		gcTestDefinitions.add(new GcTestDefinition("test_RootAge", startFile, startFile, "age"));
		gcTestDefinitions.add(new GcTestDefinition("test_RootAge1_Age2", startFile, startFile, "age#1,age#2"));
		gcTestDefinitions.add(new GcTestDefinition("test_RootAge1", startFile, folder + "age-example-delete-second.sf", "age#1"));
		gcTestDefinitions.add(new GcTestDefinition("test_RootAge2", startFile, folder + "age-example-delete-first.sf", "age#2"));
		gcTestDefinitions.add(new GcTestDefinition("test_RootEmpty", startFile, folder + "../empty-file.sf", null));
	}

}
