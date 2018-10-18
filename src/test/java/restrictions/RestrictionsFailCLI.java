package restrictions;

import common.CommonSpecificationMappingTest;

class RestrictionsFailCLI extends CommonSpecificationMappingTest {

	private static final String FOLDER = "src/test/resources/restrictionsFail/";

	/**
	 * Define test cases in constructor.
	 */
	protected RestrictionsFailCLI() {
		super(FOLDER, FOLDER + "../restrictions/restrictionsAll.sf");
	}

}
