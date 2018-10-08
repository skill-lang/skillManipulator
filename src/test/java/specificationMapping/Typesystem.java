package specificationMapping;

import common.CommonSpecificationMappingTest;

class Typesystem extends CommonSpecificationMappingTest {

	private static final String FOLDER = "src/test/resources/specificationMapper/typesystem/";

	/**
	 * Define test cases in constructor.
	 */
	protected Typesystem() {
		super(FOLDER, FOLDER + "subtypes.sf");
	}

}
