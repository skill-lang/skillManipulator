package common;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
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
import de.ust.skill.skillManipulator.internal.SkillFile;
import de.ust.skill.skillManipulator.internal.SkillState;

public abstract class CommonTest {

	protected static Path tmpFile(String string) throws Exception {
        File r = File.createTempFile(string, ".sf");
        // TODO
//        r.deleteOnExit();
        return r.toPath();
    }
	
	protected final static String sha256(String name) throws Exception {
		return sha256(new File("src/test/resources/" + name).toPath());
	}

	protected final static String sha256(Path path) throws Exception {
		byte[] bytes = Files.readAllBytes(path);
	    StringBuilder sb = new StringBuilder();
	    for (byte b : MessageDigest.getInstance("SHA-256").digest(bytes))
	    	sb.append(String.format("%02X", b));
	    return sb.toString();
	}
		
	protected static void compareSkillFiles(SkillFile sfExpected, SkillFile sfActual) {
		SkillState expectedState = (SkillState) sfExpected;
		SkillState actualState = (SkillState) sfActual;
		
		compareTypes(expectedState, actualState);
		
		compareSkillObjects(expectedState, actualState);
		
		compareStringPools(expectedState, actualState);
	}
	
	private static void compareSkillObjects(SkillState expectedState, SkillState actualState) {	
		ArrayList<StoragePool<?, ?>> expectedTypes = expectedState.getTypes();
		ArrayList<StoragePool<?, ?>> actualTypes = actualState.getTypes();
		
		for(int i = 0; i < expectedTypes.size(); i++) {
			StoragePool<?, ?> expectedType = expectedTypes.get(i);
			StoragePool<?, ?> actualType = actualTypes.get(i);
			
			StaticFieldIterator fitExpected = expectedType.fields();
			StaticFieldIterator fitActual = actualType.fields();
			while(fitExpected.hasNext()) {
				FieldDeclaration<?, ?> fieldExp = fitExpected.next();
				FieldDeclaration<?, ?> fieldAct = fitActual.next();
				
				Iterator<? extends SkillObject> expectedObjectsIt = expectedType.iterator();
				Iterator<? extends SkillObject> actualObjectsIt = actualType.iterator();
				while(expectedObjectsIt.hasNext()) {
					SkillObject expObj = expectedObjectsIt.next();
					Assertions.assertTrue(actualObjectsIt.hasNext(), "Actual SkillFile misses " + expObj);	
					SkillObject actObj = actualObjectsIt.next();
					Assertions.assertEquals(expObj.skillName(), actObj.skillName());
					
					if(!expObj.isDeleted() && !actObj.isDeleted()) {
						Object dataExpected = fieldExp.get(expObj);
						Object dataActual = fieldAct.get(actObj);
						if(!objectsEqual(dataExpected, dataActual)) {
							fail("SkillObjects " + expObj + " and " + actObj + " differ in field " + fieldExp + " (" + dataExpected + "; " + dataActual + ")");
						}
					}
				}
			}
		}
	}

	private static boolean objectsEqual(Object dataExpected, Object dataActual) {
		if(null == dataExpected && null == dataActual) return true;

		if(null == dataExpected && dataActual instanceof SkillObject && ((SkillObject)dataActual).isDeleted()) {
			return true;
		}
		
		if(null == dataExpected ^ null == dataActual) return false;
		
		if(dataExpected instanceof Comparable<?>) {
			return dataExpected.equals(dataActual);
		} else if(dataExpected instanceof SkillObject) {
			return dataExpected.toString().equals(dataActual.toString());
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
			System.out.println(dataExpected);
		}
		return false;
		
	}

	private static void compareTypes(SkillState stateExpected, SkillState stateActual) {
		ArrayList<StoragePool<?,?>> typesExpected = stateExpected.getTypes();
		ArrayList<StoragePool<?,?>> typesActual = stateActual.getTypes();
		
		Assertions.assertEquals(typesExpected.size(), typesActual.size());
		for(int i = 0; i < typesExpected.size(); i++) {
			StoragePool<?, ?> expectedType = typesExpected.get(i);
			StoragePool<?, ?> actualType = typesActual.get(i);
			Assertions.assertEquals(expectedType.typeID, actualType.typeID);
			Assertions.assertEquals(expectedType.name(), actualType.name());
			Assertions.assertEquals(expectedType.size(), actualType.size());
			compareFields(expectedType.allFields(), actualType.allFields());
		}
		
	}

	private static void compareFields(FieldIterator expFieldsIt, FieldIterator actFieldsIt) {
		while(expFieldsIt.hasNext()) {
			Assertions.assertTrue(actFieldsIt.hasNext());
			FieldDeclaration<?, ?> expField = expFieldsIt.next();
			FieldDeclaration<?, ?> actField = actFieldsIt.next();
			Assertions.assertEquals(expField, actField);
			compareFieldRestrictions(expField, actField);
		}
		if(actFieldsIt.hasNext()) {
			System.out.println(actFieldsIt.next());
		}
		Assertions.assertFalse(actFieldsIt.hasNext(), "Type has too much fields");
	}

	private static void compareFieldRestrictions(FieldDeclaration<?, ?> expField, FieldDeclaration<?, ?> actField) {
		boolean found;
		for(FieldRestriction<?> expRest : expField.restrictions) {
			found = false;
			for(FieldRestriction<?> actRest : actField.restrictions) {
				if(expRest.equals(actRest)) found = true;
			}
			Assertions.assertTrue(found, "Restriction " + expRest + " missing for field " + expField);
		}
		Assertions.assertEquals(expField.restrictions.size(), actField.restrictions.size());
	}

	private static void compareStringPools(SkillState stateExpected, SkillState stateActual) {
		stateExpected.collectStrings();
		stateActual.collectStrings();
		StringAccess expectedStrings = stateExpected.Strings();
		StringAccess actualStrings = stateActual.Strings();
		Assertions.assertEquals(expectedStrings.size(), actualStrings.size());
		for(String s : expectedStrings) {
			Assertions.assertTrue(actualStrings.contains(s), "String " + s + " not found");
		}
	}
}
