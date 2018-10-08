package orderRelation;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import common.CommonTest;
import common.SkillfileComparator;
import de.ust.skill.common.java.internal.FieldDeclaration;
import de.ust.skill.common.java.internal.StoragePool;
import de.ust.skill.manipulator.internal.SkillFile;
import de.ust.skill.manipulator.internal.SkillState;
import de.ust.skill.manipulator.orderRelation.SkillObjectComparator;
import de.ust.skill.manipulator.orderRelation.SkillObjectSorter;

class TestFramework extends CommonTest {

	/**
	 * Test sorting with nodeid comparator.
	 * 
	 * @throws Exception
	 */
	@Test
	void testFixedComparator() throws Exception {
		Path path = tmpFile("fixedComparator");
		
		SkillFile sf = SkillFile.open("src/test/resources/simpleIml/simple.iml.sf");
		sf.changePath(path);
		
		SkillObjectComparator comp = new NodeidComparator(sf);
		SkillObjectSorter.sort(sf, comp);
		
		sf.close();
		
		SkillFile sfExpected = SkillFile.open("src/test/resources/orderRelation/simpleImlSortedWithNodeid.sf");
		SkillFile sfActual = SkillFile.open(path);
		SkillfileComparator.compareSkillFiles(sfExpected, sfActual);
		
	}
	
	/**
	 * Same test as with nodeid comparator, but in this case the field has to be given in the constructor of
	 * the comparator.
	 * 
	 * @throws Exception
	 */
	@Test
	void testVariableComparator() throws Exception {
		Path path = tmpFile("variableComparator");
		
		SkillFile sf = SkillFile.open("src/test/resources/simpleIml/simple.iml.sf");
		sf.changePath(path);
		
		StoragePool<?, ?> storable = ((SkillState)sf).pool("storable");
		FieldDeclaration<?,?> nodeid = null;
		for(FieldDeclaration<?, ?> f : storable.dataFields) {
			if(f.name().equals("nodeid")) nodeid = f;
		}
		SkillObjectComparator comp = new SimpleFieldComparator<>(nodeid);
		SkillObjectSorter.sort(sf, comp);
		
		sf.close();
		
		SkillFile sfExpected = SkillFile.open("src/test/resources/orderRelation/simpleImlSortedWithNodeid.sf");
		SkillFile sfActual = SkillFile.open(path);
		SkillfileComparator.compareSkillFiles(sfExpected, sfActual);
		
	}
	
	/**
	 * Same test as the two above, but now the comparator is put in a map.
	 * 
	 * @throws Exception
	 */
	@Test
	void testMapSingleComparator() throws Exception {
		Path path = tmpFile("mapComparator");
		
		SkillFile sf = SkillFile.open("src/test/resources/simpleIml/simple.iml.sf");
		sf.changePath(path);
		
		StoragePool<?, ?> storable = ((SkillState)sf).pool("storable");
		FieldDeclaration<?,?> nodeid = null;
		for(FieldDeclaration<?, ?> f : storable.dataFields) {
			if(f.name().equals("nodeid")) nodeid = f;
		}
		SkillObjectComparator comp = new SimpleFieldComparator<>(nodeid);
		Map<String,SkillObjectComparator> compMap = new HashMap<>();
		compMap.put("storable", comp);
		SkillObjectSorter.sort(sf, compMap);
		
		sf.close();
		
		SkillFile sfExpected = SkillFile.open("src/test/resources/orderRelation/simpleImlSortedWithNodeid.sf");
		SkillFile sfActual = SkillFile.open(path);
		SkillfileComparator.compareSkillFiles(sfExpected, sfActual);
		
	}
	
	/**
	 * Test the SkillIdComparator.
	 * 
	 * @throws Exception
	 */
	@Test
	void testSkillIdComparator() throws Exception {
		Path path = tmpFile("skillIdComparator");
		
		SkillFile sf = SkillFile.open("src/test/resources/simpleIml/simple.iml.sf");
		sf.changePath(path);
		
		StoragePool<?, ?> storable = ((SkillState)sf).pool("imlroot");
		FieldDeclaration<?,?> sloc = null;
		for(FieldDeclaration<?, ?> f : storable.dataFields) {
			if(f.name().equals("sloc")) sloc = f;
		}
		SkillObjectComparator comp = new SkillIdComparator(sloc);
		SkillObjectSorter.sort(sf, comp);
		
		sf.close();
		
		SkillFile sfExpected = SkillFile.open("src/test/resources/orderRelation/simpleImlSortedWithSloc.sf");
		SkillFile sfActual = SkillFile.open(path);
		SkillfileComparator.compareSkillFiles(sfExpected, sfActual);
		
	}
	
	/**
	 * Test multiple comparators in the map.
	 * 
	 * @throws Exception
	 */
	@Test
	void testMultipleComparator() throws Exception {
		Path path = tmpFile("multipleComparator");
		
		SkillFile sf = SkillFile.open("src/test/resources/simpleIml/simple.iml.sf");
		sf.changePath(path);
		
		StoragePool<?, ?> sloc = ((SkillState)sf).pool("sloc");
		FieldDeclaration<?,?> linearline = null;
		for(FieldDeclaration<?, ?> f : sloc.dataFields) {
			if(f.name().equals("linearline")) linearline = f;
		}
		SkillObjectComparator comp = new SimpleFieldComparator<>(linearline);
		
		StoragePool<?, ?> storable = ((SkillState)sf).pool("storable");
		FieldDeclaration<?,?> nodeid = null;
		for(FieldDeclaration<?, ?> f : storable.dataFields) {
			if(f.name().equals("nodeid")) nodeid = f;
		}
		SkillObjectComparator comp2 = new SimpleFieldComparator<>(nodeid);
		
		Map<String,SkillObjectComparator> compMap = new HashMap<>();
		compMap.put("sloc", comp);
		compMap.put("storable", comp2);
		
		SkillObjectSorter.sort(sf, compMap);
		
		sf.close();
		
		SkillFile sfExpected = SkillFile.open("src/test/resources/orderRelation/multipleComparators.sf");
		SkillFile sfActual = SkillFile.open(path);
		SkillfileComparator.compareSkillFiles(sfExpected, sfActual);
		
	}

}
