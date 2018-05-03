package common;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.Map;

import org.junit.jupiter.api.Assertions;

import de.ust.skill.common.java.api.Access;
import de.ust.skill.common.java.api.StringAccess;
import de.ust.skill.common.java.internal.FieldDeclaration;
import de.ust.skill.common.java.internal.FieldIterator;
import de.ust.skill.common.java.internal.LazyField;
import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.skillManipulator.SkillFile;

public abstract class CommonTest {

	protected static Path tmpFile(String string) throws Exception {
        File r = File.createTempFile(string, ".sf");
        // r.deleteOnExit();
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
		compareTypes(sfExpected.allTypes(), sfActual.allTypes());
		
		compareSkillObjects(sfExpected, sfActual);
		
		compareStringPools(sfExpected.Strings(), sfActual.Strings());
	}

	private static void compareSkillObjects(SkillFile sfExpected, SkillFile sfActual) {
		Iterator<? extends Access<? extends SkillObject>> actualTypesIt = sfActual.allTypes().iterator();
		
		for(Access<? extends SkillObject> typeExpected : sfExpected.allTypes()) {
			Access<? extends SkillObject> typeActual = actualTypesIt.next();
			Iterator<? extends SkillObject> actualObjectsIt = typeActual.iterator();
			
			for(SkillObject objExpected : typeExpected) {
				SkillObject objActual = actualObjectsIt.next();
				Assertions.assertEquals(objExpected.skillName(), objActual.skillName());
				
				FieldIterator fitExpected = typeExpected.allFields();
				FieldIterator fitActual = typeActual.allFields();
				while(fitExpected.hasNext()) {
					FieldDeclaration<?, ?> fieldExp = fitExpected.next();
					FieldDeclaration<?, ?> fieldAct = fitActual.next();
					
					if(fieldExp instanceof LazyField<?, ?>) {
						((LazyField<?, ?>)fieldExp).ensureLoaded();
					}
					if(fieldAct instanceof LazyField<?, ?>) {
						((LazyField<?, ?>)fieldAct).ensureLoaded();
					}
					
					Object dataExpected = fieldExp.get(objExpected);
					Object dataActual = fieldAct.get(objActual);
					if(!objectsEqual(dataExpected, dataActual)) {
						fail("SkillObjects " + objExpected + " and " + objActual + " differ in field " + fieldExp.name() + " (" + dataExpected + "; " + dataActual + ")");
					}
				}
			}
		}
		
	}

	private static boolean objectsEqual(Object dataExpected, Object dataActual) {
		if(null == dataExpected && null == dataActual) {
			return true;
		}
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
			return ((Map<?, ?>)dataExpected).equals((Map<?, ?>)dataActual);
		} else {
			System.out.println(dataExpected);
		}
		return false;
		
	}

	private static void compareTypes(Iterable<? extends Access<? extends SkillObject>> expectedTypes,
			Iterable<? extends Access<? extends SkillObject>> actualTypes) {
		Iterator<? extends Access<? extends SkillObject>> expIt = expectedTypes.iterator();
		Iterator<? extends Access<? extends SkillObject>> actIt = actualTypes.iterator();
		while(expIt.hasNext()) {
			Assertions.assertTrue(actIt.hasNext());
			Access<? extends SkillObject> expType = expIt.next();
			Access<? extends SkillObject> actType = actIt.next();
			Assertions.assertEquals(expType.toString(), actType.toString());
			compareFields(expType.allFields(), actType.allFields());
		}
		Assertions.assertFalse(actIt.hasNext());
	}

	private static void compareFields(FieldIterator expFieldsIt, FieldIterator actFieldsIt) {
		while(expFieldsIt.hasNext()) {
			Assertions.assertTrue(actFieldsIt.hasNext());
			FieldDeclaration<?, ?> expField = expFieldsIt.next();
			FieldDeclaration<?, ?> actField = actFieldsIt.next();
			Assertions.assertEquals(expField.type().toString(), actField.type().toString());
			Assertions.assertEquals(expField.toString(), actField.toString());
			Assertions.assertIterableEquals(expField.restrictions, actField.restrictions);
		}
		Assertions.assertFalse(actFieldsIt.hasNext());
	}

	private static void compareStringPools(StringAccess expectedStrings, StringAccess actualStrings) {
		Assertions.assertEquals(expectedStrings.size(), actualStrings.size());
		for(String s : expectedStrings) {
			Assertions.assertTrue(actualStrings.contains(s), "String " + s + " not found");
		}
	}
}
