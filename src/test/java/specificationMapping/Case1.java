package specificationMapping;

import common.CommonSpecificationMappingTest;

class Case1 extends CommonSpecificationMappingTest {

	private static final String FOLDER = "src/test/resources/specificationMapper/case1/";

	/**
	 * Define test cases in constructor.
	 */
	protected Case1() {
		super(FOLDER, FOLDER + "specification.sf");
	}
}
