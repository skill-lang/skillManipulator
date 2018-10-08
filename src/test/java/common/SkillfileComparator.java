package common;

import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.jupiter.api.Assertions;

import de.ust.skill.common.java.api.StringAccess;
import de.ust.skill.common.java.internal.FieldDeclaration;
import de.ust.skill.common.java.internal.FieldIterator;
import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.common.java.internal.StaticFieldIterator;
import de.ust.skill.common.java.internal.StoragePool;
import de.ust.skill.common.java.restrictions.FieldRestriction;
import de.ust.skill.common.java.restrictions.TypeRestriction;
import de.ust.skill.manipulator.internal.SkillFile;
import de.ust.skill.manipulator.internal.SkillState;

/**
 * Compare Skillfiles for structural equality.
 * 
 * @author olibroe
 *
 */
public class SkillfileComparator {

	/**
	 * Compare the two given Skillfiles for structural equality.
	 * 
	 * @param sfExpected
	 * @param sfActual
	 */
	public static void compareSkillFiles(SkillFile sfExpected, SkillFile sfActual) {
		SkillState expectedState = (SkillState) sfExpected;
		SkillState actualState = (SkillState) sfActual;
		
		// compare typesystem
		compareTypes(expectedState, actualState);
		
		// compare objects and their field values
		compareSkillObjects(expectedState, actualState);
		
		// compare strings
		compareStringPools(expectedState, actualState);
	}

	/**
	 * Run through all fields of old and new typesystem and compare the value of every type.
	 * 
	 * @param expectedState
	 * @param actualState
	 */
	private static void compareSkillObjects(SkillState expectedState, SkillState actualState) {	
		ArrayList<StoragePool<?, ?>> expectedTypes = expectedState.getTypes();
		ArrayList<StoragePool<?, ?>> actualTypes = actualState.getTypes();
		
		// loop over types
		for(int i = 0; i < expectedTypes.size(); i++) {
			StoragePool<?, ?> expectedType = expectedTypes.get(i);
			StoragePool<?, ?> actualType = actualTypes.get(i);
			
			// loop over fields
			StaticFieldIterator fitExpected = expectedType.fields();
			StaticFieldIterator fitActual = actualType.fields();
			while(fitExpected.hasNext()) {
				FieldDeclaration<?, ?> fieldExp = fitExpected.next();
				FieldDeclaration<?, ?> fieldAct = fitActual.next();
				
				// loop over objects
				Iterator<? extends SkillObject> expectedObjectsIt = expectedType.iterator();
				Iterator<? extends SkillObject> actualObjectsIt = actualType.iterator();
				while(expectedObjectsIt.hasNext()) {
					SkillObject expObj = expectedObjectsIt.next();
					Assertions.assertTrue(actualObjectsIt.hasNext(), "Actual SkillFile misses " + expObj);	
					SkillObject actObj = actualObjectsIt.next();
					// compare object names
					Assertions.assertEquals(expObj.skillName(), actObj.skillName());
					
					if(!expObj.isDeleted() && !actObj.isDeleted()) {
						// get field values
						Object dataExpected = fieldExp.get(expObj);
						Object dataActual = fieldAct.get(actObj);
						
						// compare field values for equality
						if(!objectsEqual(dataExpected, dataActual)) {
							fail("SkillObjects " + expObj + " and " + actObj + " differ in field " + fieldExp + " (" + dataExpected + "; " + dataActual + ")");
						}
					}
				}
			}
		}
	}

	/**
	 * Checks if given objects are equal.
	 * We need a method here, because the data types have different definitions of equality.
	 * 
	 * @param dataExpected
	 * @param dataActual
	 * @return
	 */
	private static boolean objectsEqual(Object dataExpected, Object dataActual) {
		if(null == dataExpected && null == dataActual) return true;

		if(null == dataExpected && dataActual instanceof SkillObject && ((SkillObject)dataActual).isDeleted()) {
			return true;
		}

		if(null == dataExpected ^ null == dataActual) return false;
		
		// all ground types except annotation are instances of comparable
		if(dataExpected instanceof Comparable<?>) {
			return dataExpected.equals(dataActual);
		// Skillobjects are compared by name
		} else if(dataExpected instanceof SkillObject) {
			return dataExpected.toString().equals(dataActual.toString());
		// iterables
		} else if(dataExpected instanceof Iterable<?>) {
			Iterator<?> dataActIt = ((Iterable<?>)dataActual).iterator();
			while(dataActIt.hasNext()) {
				boolean objFound = false;
				Object actObj = dataActIt.next();
				for(Object expObj : (Iterable<?>)dataExpected) {
					if(objectsEqual(expObj, actObj)) objFound = true;
				}
				if(!objFound) return false;
			}
			return true;
		// comparison of maps
		} else if(dataExpected instanceof Map<?, ?>) {			
			for(Entry<?, ?> entryExp : ((Map<?, ?>) dataExpected).entrySet()) {
				boolean objFound = false;
				for(Entry<?, ?> entryAct : ((Map<?, ?>) dataActual).entrySet()) {
					if(objectsEqual(entryExp.getKey(), entryAct.getKey())) {
						objFound = objectsEqual(entryExp.getValue(), entryAct.getValue());
					}
				}
				if(!objFound) return false;
			}
			return true;
		} else {
			// this should not happen
			System.out.println(dataExpected);
		}
		return false;
		
	}

	/**
	 * Compare types of the typesystem.
	 * 
	 * @param stateExpected
	 * @param stateActual
	 */
	private static void compareTypes(SkillState stateExpected, SkillState stateActual) {
		ArrayList<StoragePool<?,?>> typesExpected = stateExpected.getTypes();
		ArrayList<StoragePool<?,?>> typesActual = stateActual.getTypes();
		
		// check number of types
		Assertions.assertEquals(typesExpected.size(), typesActual.size());
		
		// loop over types
		for(int i = 0; i < typesExpected.size(); i++) {
			StoragePool<?, ?> expectedType = typesExpected.get(i);
			StoragePool<?, ?> actualType = typesActual.get(i);
			
			// compare name, typeID and object number
			Assertions.assertEquals(expectedType.typeID, actualType.typeID);
			Assertions.assertEquals(expectedType.name(), actualType.name());
			Assertions.assertEquals(expectedType.size(), actualType.size());
			
			// compare type restrictions
			compareTypeRestrictions(expectedType, actualType);
			// compare field definitions
			compareFields(expectedType.allFields(), actualType.allFields());
		}
		
	}

	/**
	 * Compare type restrictions. Order of restrictions is not defined.
	 * Implementation is not efficient, but we do not expect huge amount of restrictions.
	 * 
	 * @param expectedType
	 * @param actualType
	 */
	private static void compareTypeRestrictions(StoragePool<?, ?> expectedType, StoragePool<?, ?> actualType) {
		boolean found;
		// loop over expected restrictions
		for(TypeRestriction expRest : expectedType.restrictions) {
			found = false;
			// search expected restriction in actual restrictions
			for(TypeRestriction actRest : actualType.restrictions) {
				if(expRest.equals(actRest)) found = true;
			}
			Assertions.assertTrue(found, "Restriction " + expRest + " missing for type " + expectedType);
		}
		Assertions.assertEquals(expectedType.restrictions.size(), actualType.restrictions.size());
	}

	/**
	 * Compare fields.
	 * 
	 * @param expFieldsIt
	 * @param actFieldsIt
	 */
	private static void compareFields(FieldIterator expFieldsIt, FieldIterator actFieldsIt) {
		// loop over fields
		while(expFieldsIt.hasNext()) {
			Assertions.assertTrue(actFieldsIt.hasNext());
			FieldDeclaration<?, ?> expField = expFieldsIt.next();
			FieldDeclaration<?, ?> actField = actFieldsIt.next();
			
			// FieldDeclaration defines a equals method
			Assertions.assertEquals(expField, actField);
			// compare field restrictions
			compareFieldRestrictions(expField, actField);
		}
		Assertions.assertFalse(actFieldsIt.hasNext(), "Type has too much fields");
	}

	/**
	 * Compare field restrictions.
	 * Not efficient, but we do not expect a big amount of restrictions.
	 * 
	 * @param expField
	 * @param actField
	 */
	private static void compareFieldRestrictions(FieldDeclaration<?, ?> expField, FieldDeclaration<?, ?> actField) {
		boolean found;
		// loop over expected restrictions
		for(FieldRestriction<?> expRest : expField.restrictions) {
			found = false;
			// search expected restriction in actual restrictions
			for(FieldRestriction<?> actRest : actField.restrictions) {
				if(expRest.equals(actRest)) found = true;
			}
			Assertions.assertTrue(found, "Restriction " + expRest + " missing for field " + expField);
		}
		Assertions.assertEquals(expField.restrictions.size(), actField.restrictions.size());
	}

	/**
	 * Compare the strings of both states.
	 * 
	 * @param stateExpected
	 * @param stateActual
	 */
	private static void compareStringPools(SkillState stateExpected, SkillState stateActual) {
		// make sure that all strings are loaded
		stateExpected.collectStrings();
		stateActual.collectStrings();
		
		StringAccess expectedStrings = stateExpected.Strings();
		StringAccess actualStrings = stateActual.Strings();
		
		// compare first the size and then all strings for equality
		Assertions.assertEquals(expectedStrings.size(), actualStrings.size());
		for(String s : expectedStrings) {
			Assertions.assertTrue(actualStrings.contains(s), "String " + s + " not found");
		}
	}
	
}
