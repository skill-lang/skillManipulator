package garbageCollection;

import common.CommonGcTest;

class EmptyTypePoolTest extends CommonGcTest{
	private final static String folder = "src/test/resources/emptyTypePool/";
	
	/**
	 * Define test cases in constructor.
	 */
	protected EmptyTypePoolTest() {
		gcTestDefinitions.add(new GcTestDefinition("test_reference_RootT", folder + "reference/reference.sf",
				folder + "reference/result.sf", "t"));
		gcTestDefinitions.add(new GcTestDefinition("test_reference_RootT_keepColl", folder + "reference/reference.sf",
				folder + "reference/result.sf", "t", true));
		gcTestDefinitions.add(new GcTestDefinition("test_referenceVar_RootT", folder + "reference/reference_variation.sf",
				folder + "reference/result_variation.sf", "u"));
		
		gcTestDefinitions.add(new GcTestDefinition("test_array_RootT", folder + "array/array.sf",
				folder + "array/result.sf", "t"));
		gcTestDefinitions.add(new GcTestDefinition("test_array_RootT_keepCool", folder + "array/array.sf",
				folder + "array/array.sf", "t", true));
		
		gcTestDefinitions.add(new GcTestDefinition("test_map_RootT", folder + "map/map.sf",
				folder + "map/result.sf", "t"));
		gcTestDefinitions.add(new GcTestDefinition("test_map_RootT_keepCool", folder + "map/map.sf",
				folder + "map/map.sf", "t", true));
		
	}
}
