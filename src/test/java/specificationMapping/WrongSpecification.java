package specificationMapping;

import common.CommonSpecificationMappingTest;

class WrongSpecification extends CommonSpecificationMappingTest {

	private static final String FOLDER = "src/test/resources/specificationMapper/wrongSpecifications/";
	private static final String STARTFILE = "src/test/resources/empty-file.sf";

	/**
	 * Define test cases in constructor.
	 */
	protected WrongSpecification() {
		super(FOLDER, STARTFILE);
	}

}
