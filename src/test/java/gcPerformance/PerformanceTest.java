package gcTest;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import common.CommonTest;
import de.ust.skill.common.java.api.Access;
import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.skillManipulator.GarbageCollector;
import de.ust.skill.skillManipulator.GarbageCollector.CollectionRoot;
import de.ust.skill.skillManipulator.SkillFile;

class PerformanceTest extends CommonTest{
	private static final String FOLDER = "src/test/resources/performance/";
	private static final int EXECUTIONS = 1;
	private static final boolean TEST_CORRECTNESS = true;
	
	class TimeInformation {
		long[] time = new long[EXECUTIONS];
		int totalObjects = -1;
		String filename;
		
		public TimeInformation(String filename) throws Exception {
			this.filename = filename;
		}
	}

	private static Set<TimeInformation> timeInfos = new HashSet<>();
	
	@Test
	void testBash() throws Exception {
		TimeInformation ti = new TimeInformation("bash.iml.sf");
		timeInfos.add(ti);
		
        executeGC(ti);
	}
	
	@Test
	void testPython() throws Exception {
		TimeInformation ti = new TimeInformation("python.iml.sf");
		timeInfos.add(ti);
		
        executeGC(ti);
	}
	
	@Test
	void testGrep() throws Exception {
		TimeInformation ti = new TimeInformation("grep.iml.sf");
		timeInfos.add(ti);
		
        executeGC(ti);
	}
	
	@Test
	void testBluefish() throws Exception {
		TimeInformation ti = new TimeInformation("bluefish.iml.sf");
		timeInfos.add(ti);
		
        executeGC(ti);
	}
	
	@Test
	void testSed() throws Exception {
		TimeInformation ti = new TimeInformation("sed.iml.sf");
		timeInfos.add(ti);
		
        executeGC(ti);
	}
	
	@Test
	void testBison() throws Exception {
		TimeInformation ti = new TimeInformation("bison.iml.sf");
		timeInfos.add(ti);
		
        executeGC(ti);
	}
	
	@Test
	void testGnugo() throws Exception {
		TimeInformation ti = new TimeInformation("gnugo.iml.sf");
		timeInfos.add(ti);
		
        executeGC(ti);
	}
	
	@Test
	void testGqview() throws Exception {
		TimeInformation ti = new TimeInformation("gqview.iml.sf");
		timeInfos.add(ti);
		
        executeGC(ti);
	}
	
	@Test
	void testDc() throws Exception {
		TimeInformation ti = new TimeInformation("dc.iml.sf");
		timeInfos.add(ti);
		
        executeGC(ti);
	}
	
	@Test
	void testTime() throws Exception {
		TimeInformation ti = new TimeInformation("time.iml.sf");
		timeInfos.add(ti);
		
        executeGC(ti);
	}
	
	private void executeGC(TimeInformation ti) throws Exception {
//		if(Runtime.getRuntime().maxMemory() < 4E9) {
//			System.out.println("Please give more memory");
//			return;
//		}
		boolean firstTime = true;
		for(int i = 0; i < EXECUTIONS; i++) {
			SkillFile sf = SkillFile.open(FOLDER + ti.filename);
	        sf.changePath(tmpFile(ti.filename));

	        if(firstTime) {
		        int objCount = 0;
		        for(Access<? extends SkillObject> t : sf.allTypes()) {
		        	if(t.superName() == null) objCount += t.size();
		        }
		        ti.totalObjects = objCount;
	        }
	        
	        Set<CollectionRoot> roots = new HashSet<>();
	        roots.add(new CollectionRoot("imlgraph"));

	        long start = System.currentTimeMillis();
	        GarbageCollector.run(sf, roots, false, false, false);
	        long end = System.currentTimeMillis();
	        
	        ti.time[i] = end - start;

	        if(firstTime && TEST_CORRECTNESS) {
		        sf.close();
	
//		        SkillFile sfActual = SkillFile.open(sf.currentPath());
		        SkillFile sfExpected = SkillFile.open(FOLDER + "expected/" + ti.filename);
		        compareSkillFiles(sfExpected, sf);
		           
	        }
	        
	        firstTime = false;
		}
	}
	
	@AfterAll
	static void performanceAnalysis() {
		for(TimeInformation ti : timeInfos) {
			System.out.println(ti.filename);
			System.out.println("  Object count: " + ti.totalObjects);
			double average = 0;
			for(long t : ti.time) {
				System.out.println("  " + t + " ms");
				average += t;
			}
			average = average / ti.time.length;
			System.out.println("  Average: " + average + " ms");
			double timePerObject = average / ti.totalObjects * 1000;
			System.out.println("  Time per object: " + timePerObject + " ns");
		}

//		deleteTempSkillFiles();
	}

}
