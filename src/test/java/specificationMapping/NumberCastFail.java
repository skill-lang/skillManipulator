package specificationMapping;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import common.CommonSpecificationMappingTest;
import de.ust.skill.manipulator.specificationMapping.SpecificationMapper;

class NumberCastFail extends CommonSpecificationMappingTest {

	private static final String FOLDER = "src/test/resources/specificationMapper/numberCastFail/";
	private static final String STARTFILE = FOLDER + "specification.sf";

	/**
	 * Define test cases in constructor.
	 */
	protected NumberCastFail() {
		super(FOLDER, STARTFILE);
	}
	
	/**
	 * Casting to byte fails.
	 * 
	 * @throws Exception
	 */
	@Test
	public void byteFail() throws Exception {
		SpecificationMapper mapper = executeMapping(STARTFILE, FOLDER + "byteFail.skill");
		
		Set<String> expectedFailFields = new HashSet<>();
		expectedFailFields.add("a.b->a.b");
		expectedFailFields.add("a.s->a.s");
		expectedFailFields.add("a.bignegdouble->a.bignegdouble");
		expectedFailFields.add("a.bigposdouble->a.bigposdouble");
		expectedFailFields.add("a.nan->a.nan");
		expectedFailFields.add("a.maxlong->a.maxlong");
		expectedFailFields.add("a.minlong->a.minlong");
		
		compareFailingFields(mapper, expectedFailFields);
	}
	
	/**
	 * Casting to double fails.
	 * 
	 * @throws Exception
	 */
	@Test
	public void doubleFail() throws Exception {
		SpecificationMapper mapper = executeMapping(STARTFILE, FOLDER + "doubleFail.skill");
		
		Set<String> expectedFailFields = new HashSet<>();
		expectedFailFields.add("a.b->a.b");
		expectedFailFields.add("a.s->a.s");
		
		compareFailingFields(mapper, expectedFailFields);
	}
	
	/**
	 * Casting to float fails.
	 * 
	 * @throws Exception
	 */
	@Test
	public void floatFail() throws Exception {
		SpecificationMapper mapper = executeMapping(STARTFILE, FOLDER + "floatFail.skill");
		
		Set<String> expectedFailFields = new HashSet<>();
		expectedFailFields.add("a.b->a.b");
		expectedFailFields.add("a.s->a.s");
		
		compareFailingFields(mapper, expectedFailFields);
	}
	
	/**
	 * Casting to int fails.
	 * 
	 * @throws Exception
	 */
	@Test
	public void intFail() throws Exception {
		SpecificationMapper mapper = executeMapping(STARTFILE, FOLDER + "intFail.skill");
		
		Set<String> expectedFailFields = new HashSet<>();
		expectedFailFields.add("a.maxlong->a.maxlong");
		expectedFailFields.add("a.minlong->a.minlong");
		expectedFailFields.add("a.bignegdouble->a.bignegdouble");
		expectedFailFields.add("a.bigposdouble->a.bigposdouble");
		expectedFailFields.add("a.nan->a.nan");
		expectedFailFields.add("a.b->a.b");
		expectedFailFields.add("a.s->a.s");	
		
		compareFailingFields(mapper, expectedFailFields);
	}
	
	/**
	 * Casting to long fails.
	 * 
	 * @throws Exception
	 */
	@Test
	public void longFail() throws Exception {
		SpecificationMapper mapper = executeMapping(STARTFILE, FOLDER + "longFail.skill");
		
		Set<String> expectedFailFields = new HashSet<>();
		expectedFailFields.add("a.bignegdouble->a.bignegdouble");
		expectedFailFields.add("a.bigposdouble->a.bigposdouble");
		expectedFailFields.add("a.nan->a.nan");
		expectedFailFields.add("a.b->a.b");
		expectedFailFields.add("a.s->a.s");	
		
		compareFailingFields(mapper, expectedFailFields);
	}
	
	/**
	 * Casting to short fails.
	 * 
	 * @throws Exception
	 */
	@Test
	public void shortFail() throws Exception {
		SpecificationMapper mapper = executeMapping(STARTFILE, FOLDER + "shortFail.skill");
		
		Set<String> expectedFailFields = new HashSet<>();
		expectedFailFields.add("a.maxlong->a.maxlong");
		expectedFailFields.add("a.minlong->a.minlong");
		expectedFailFields.add("a.bignegdouble->a.bignegdouble");
		expectedFailFields.add("a.bigposdouble->a.bigposdouble");
		expectedFailFields.add("a.nan->a.nan");
		expectedFailFields.add("a.b->a.b");
		expectedFailFields.add("a.s->a.s");	
		
		compareFailingFields(mapper, expectedFailFields);
	}
	
}
