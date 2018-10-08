package specificationMapping;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import common.CommonSpecificationMappingTest;
import de.ust.skill.manipulator.specificationMapping.SpecificationMapper;

class Fieldtransfer extends CommonSpecificationMappingTest {

	private static final String FOLDER = "src/test/resources/specificationMapper/fieldtransfer/";
	private static final String STARTFILE = FOLDER + "specification.sf";

	/**
	 * Define test cases in constructor.
	 */
	protected Fieldtransfer() {
		super(FOLDER, STARTFILE);
	}
	
	/**
	 * Additional test with a lot of failing casts.
	 * 
	 * @throws Exception
	 */
	@Test
	public void failingCasts() throws Exception {
		SpecificationMapper mapper = executeMapping(STARTFILE, FOLDER + "failingCastsTest.skill");
		
		Set<String> expectedFailFields = new HashSet<>();
		expectedFailFields.add("u.a->u.a");
		expectedFailFields.add("u.d->u.d");
		expectedFailFields.add("v.a->v.a");
		expectedFailFields.add("v.e->v.e");
		expectedFailFields.add("t.g->t.g");
		expectedFailFields.add("t.l->t.l");
		expectedFailFields.add("v.d->v.d");
		expectedFailFields.add("t.f->t.f");
		expectedFailFields.add("t.n->t.n");
		expectedFailFields.add("t.m->t.m");
		expectedFailFields.add("t.i->t.i");
		expectedFailFields.add("u.e->u.e");
		expectedFailFields.add("u.c->u.c");
		
		compareFailingFields(mapper, expectedFailFields);
	}
}
