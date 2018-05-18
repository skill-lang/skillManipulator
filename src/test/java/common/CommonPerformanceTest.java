package common;

import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;

import de.ust.skill.common.java.api.Access;
import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.skillManipulator.GarbageCollector;
import de.ust.skill.skillManipulator.SkillFile;
import de.ust.skill.skillManipulator.GarbageCollector.CollectionRoot;

public abstract class CommonPerformanceTest extends CommonTest{
	private final int executions;
	private final String expectedFolder;
	private static Set<TimeInformation> timeInfos = new HashSet<>();
	
	public CommonPerformanceTest(int executions, String expectedFolder) {
		this.executions = executions;
		this.expectedFolder = expectedFolder;
	}
	
	class TimeInformation {
		long[] time = new long[executions];
		int totalObjects = -1;
		String filename;
		
		public TimeInformation(String filename) throws Exception {
			this.filename = filename;
		}
	}

	protected void executeGC(Path filepath) throws Exception {
		String filename = filepath.getFileName().toString();
		
		TimeInformation ti = new TimeInformation(filename);
		timeInfos.add(ti);
		
		System.out.println("GC for " + filename);
		
		boolean firstTime = true;
		for(int i = 0; i < executions; i++) {
			System.out.print("  run " + (i+1) + " of " + executions + " started...");
			
			SkillFile sf = SkillFile.open(filepath);
	        sf.changePath(tmpFile(filename));

	        if(firstTime) ti.totalObjects = countObjects(sf);
	        
	        Set<CollectionRoot> roots = new HashSet<>();
	        roots.add(new CollectionRoot("imlgraph"));

	        long start = System.currentTimeMillis();
	        GarbageCollector.run(sf, roots, false, false, false);
	        long end = System.currentTimeMillis();
	        
	        ti.time[i] = end - start;

	        if(firstTime && expectedFolder != null) {
		        sf.close();
	
		        try {
		        	SkillFile sfExpected = SkillFile.open(expectedFolder + filename);
		        	compareSkillFiles(sfExpected, sf);       
		        } catch (Exception e) {
		        	System.out.println("Expected SkillFile could not be opened: " + expectedFolder + filename);
		        }
		        
	        }
	        
	        firstTime = false;
	        
	        System.out.println("finished (" + ti.time[i] + " ms)");
		}
	}

	private int countObjects(SkillFile sf) {
		int objCount = 0;
		for(Access<? extends SkillObject> t : sf.allTypes()) {
			if(t.superName() == null) objCount += t.size();
		}
		return objCount;
	}
	
	@AfterAll
	static void performanceAnalysis() throws Exception {
		String filename = new SimpleDateFormat("'output/performanceTest-'yyyy-MM-dd-HH-mm-ss'.csv'").format(new Date());
		PrintWriter pw = new PrintWriter(new File(filename));
        StringBuilder sb = new StringBuilder();
        sb.append("filename,object count,time(ms)\n");
	
		for(TimeInformation ti : timeInfos) {
			System.out.println(ti.filename);
			System.out.println("  Object count: " + ti.totalObjects);
			
			double average = 0;
			for(long t : ti.time) {
				sb.append(ti.filename + "," + ti.totalObjects + "," + t + "\n");
				average += t;
			}
			
			average = average / ti.time.length;
			System.out.println("  Average: " + average + " ms");
			double timePerObject = average / ti.totalObjects * 1000;
			System.out.println("  Time per object: " + timePerObject + " ns");
		}

		pw.write(sb.toString());
		pw.close();
	}


}
