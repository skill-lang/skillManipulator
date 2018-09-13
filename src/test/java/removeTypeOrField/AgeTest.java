package removeTypeOrField;

import common.CommonRemoveTest;

class AgeTest extends CommonRemoveTest{
	private static final String folder = "src/test/resources/age/";
	private static final String startfile = folder + "age-example.sf";
	
	AgeTest() {
		removeTestDefinitions.add(new RemoveFieldTestDefinition("test_RemoveFieldAge", startfile,
				folder + "age-example-without-field.sf", "age", "age"));
		removeTestDefinitions.add(new RemoveFieldTestDefinition("test_RemoveFieldOfWrongType", startfile,
				startfile, "TYPE_DOES_NOT_EXIST", "age"));
		
		removeTestDefinitions.add(new RemoveTypeTestDefinition("test_RemoveTypeAge", startfile,
				folder + "../empty-file.sf", "age"));
		removeTestDefinitions.add(new RemoveTypeTestDefinition("test_RemoveTypeNotExisting", startfile,
				startfile, "norealtype"));
	}

}
