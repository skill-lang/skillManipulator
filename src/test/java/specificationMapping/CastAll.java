package specificationMapping;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import common.CommonSpecificationMappingTest;
import de.ust.skill.manipulator.specificationMapping.SpecificationMapper;

class CastAll extends CommonSpecificationMappingTest {

	private static final String FOLDER = "src/test/resources/specificationMapper/castAll/";
	private static final String STARTFILE = FOLDER + "specification.sf";

	/**
	 * Define test cases in constructor.
	 */
	protected CastAll() {
		super(FOLDER, STARTFILE);
	}
	
	/**
	 * Annotation field type check fails.
	 * 
	 * @throws Exception
	 */
	@Test
	public void annotationCastFail() throws Exception {
		SpecificationMapper mapper = executeMapping(STARTFILE, FOLDER + "annotationCastFail.skill");
		
		Set<String> expectedFailFields = new HashSet<>();
		expectedFailFields.add("f.anno->f.anno");
		
		compareFailingFields(mapper, expectedFailFields);
	}
	
	/**
	 * Six container casts fail.
	 * 
	 * @throws Exception
	 */
	@Test
	public void containerCastFail() throws Exception {
		SpecificationMapper mapper = executeMapping(STARTFILE, FOLDER + "containerCastFail.skill");
		
		Set<String> expectedFailFields = new HashSet<>();
		expectedFailFields.add("e.d->e.d");
		expectedFailFields.add("e.j->e.j");
		expectedFailFields.add("e.a->e.a");
		expectedFailFields.add("e.b->e.b");
		expectedFailFields.add("f.i->f.i");
		expectedFailFields.add("e.c->e.c");
		
		compareFailingFields(mapper, expectedFailFields);
	}
	
	/**
	 * Five container casts fail in dynamic check.
	 * 
	 * @throws Exception
	 */
	@Test
	public void containerDynamicCheckFail() throws Exception {
		SpecificationMapper mapper = executeMapping(STARTFILE, FOLDER + "containerDynamicCheckFail.skill");
		
		Set<String> expectedFailFields = new HashSet<>();
		expectedFailFields.add("f.e->f.e");
		expectedFailFields.add("f.f->f.f");
		expectedFailFields.add("e.i->e.i");
		expectedFailFields.add("f.g->f.g");
		expectedFailFields.add("e.g->e.g");
		
		compareFailingFields(mapper, expectedFailFields);
	}
	
	/**
	 * Failing cast from groundtype to usertype.
	 * 
	 * @throws Exception
	 */
	@Test
	public void groundtypeToUsertypeFail() throws Exception {
		SpecificationMapper mapper = executeMapping(STARTFILE, FOLDER + "groundtypeToUsertypeFail.skill");
		
		Set<String> expectedFailFields = new HashSet<>();
		expectedFailFields.add("e.a->e.a");
		expectedFailFields.add("e.b->e.b");
		expectedFailFields.add("e.e->e.e");
		expectedFailFields.add("e.c->e.c");
		expectedFailFields.add("e.d->e.d");
		
		compareFailingFields(mapper, expectedFailFields);
	}
	
	/**
	 * Map cast fails because of key.
	 * 
	 * @throws Exception
	 */
	@Test
	public void mapKeyFail() throws Exception {
		SpecificationMapper mapper = executeMapping(STARTFILE, FOLDER + "mapKeyFail.skill");
		
		Set<String> expectedFailFields = new HashSet<>();
		expectedFailFields.add("e.j->e.j");
		expectedFailFields.add("f.i->f.i");
		
		compareFailingFields(mapper, expectedFailFields);
	}
	
	/**
	 * Map cast fails because of value.
	 * 
	 * @throws Exception
	 */
	@Test
	public void mapValueFail() throws Exception {
		SpecificationMapper mapper = executeMapping(STARTFILE, FOLDER + "mapKeyFail.skill");
		
		Set<String> expectedFailFields = new HashSet<>();
		expectedFailFields.add("e.j->e.j");
		expectedFailFields.add("f.i->f.i");
		
		compareFailingFields(mapper, expectedFailFields);
	}
}
