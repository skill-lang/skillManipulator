package specificationMapping;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import common.CommonSpecificationMappingTest;
import de.ust.skill.manipulator.specificationMapping.SpecificationMapper;

class UsertypeCast extends CommonSpecificationMappingTest {

	private static final String FOLDER = "src/test/resources/specificationMapper/usertypeCast/";
	private static final String STARTFILE = FOLDER + "subtypes.sf";

	protected UsertypeCast() {
		super(FOLDER, STARTFILE);
	}
	
	@Test
	public void castToGroundtypes() throws Exception {
		SpecificationMapper mapper = executeMapping(STARTFILE, FOLDER + "castToGroundtypes.skill");
		
		Set<String> expectedFailFields = new HashSet<>();
		expectedFailFields.add("c.c->c.c");
		expectedFailFields.add("d.d->d.d");
		expectedFailFields.add("b.b->b.b");
		
		compareFailingFields(mapper, expectedFailFields);
	}
	
	@Test
	public void downcastToD2() throws Exception {
		SpecificationMapper mapper = executeMapping(STARTFILE, FOLDER + "downcastToD2.skill");
		
		Set<String> expectedFailFields = new HashSet<>();
		expectedFailFields.add("b.b->b.b");
		
		compareFailingFields(mapper, expectedFailFields);
	}
	
	@Test
	public void failingDowncast() throws Exception {
		SpecificationMapper mapper = executeMapping(STARTFILE, FOLDER + "failingDowncast.skill");
		
		Set<String> expectedFailFields = new HashSet<>();
		expectedFailFields.add("b.b->b.b");
		expectedFailFields.add("a.a->a.a");
		
		compareFailingFields(mapper, expectedFailFields);
	}
}
