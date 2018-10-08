package specificationMapping;

import common.CommonSpecificationMappingTest;

class Case2 extends CommonSpecificationMappingTest {
	
	private static final String FOLDER = "src/test/resources/specificationMapper/case2/";

	/**
	 * Define test cases in constructor.
	 */
	protected Case2() {
		super(FOLDER, FOLDER + "specification.sf");
	}
}
