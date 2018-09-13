package removeTypeOrField;

import common.CommonRemoveTest;

class SimpleImlTest extends CommonRemoveTest{
	private static final String folder = "src/test/resources/simpleIml/";
	private static final String startfile = folder + "simple.iml.sf";
	
	SimpleImlTest() {
		removeTestDefinitions.add(new RemoveFieldTestDefinition("test_RemoveFieldNameOfIdentifier", startfile,
				folder + "simpleIml-removed-name-of-identifier.sf", "identifier", "name"));
		removeTestDefinitions.add(new RemoveFieldTestDefinition("test_RemoveFieldPathnameOfSloc", startfile,
				folder + "simpleIml-removed-pathname-of-sloc.sf", "sloc", "pathname"));
		removeTestDefinitions.add(new RemoveFieldTestDefinition("test_RemoveFieldSlocOfImlRoot", startfile,
				folder + "simpleIml-removed-sloc-of-imlroot.sf", "imlroot", "sloc"));
		removeTestDefinitions.add(new RemoveFieldTestDefinition("test_RemoveFieldNodeidOfWrongType", startfile,
				startfile, "imlroot", "nodeid"));
		
		removeTestDefinitions.add(new RemoveTypeTestDefinition("test_RemoveTypeAttributable", startfile,
				folder + "simpleIml-removed-attributable.sf", "attributable"));
		removeTestDefinitions.add(new RemoveTypeTestDefinition("test_RemoveTypeSloc", startfile,
				folder + "simpleIml-removed-sloc.sf", "sloc"));
		removeTestDefinitions.add(new RemoveTypeTestDefinition("test_RemoveTypeIdentifier", startfile,
				folder + "simpleIml-removed-identifier.sf", "identifier"));

	}

}
