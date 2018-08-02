package common;

import java.io.File;
import java.io.PrintWriter;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterAll;

import de.ust.skill.common.java.api.Access;
import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.skillManipulator.GarbageCollector;
import de.ust.skill.skillManipulator.SkillFile;
import de.ust.skill.skillManipulator.GarbageCollector.CollectionRoot;

public abstract class CommonPerformanceTest extends CommonTest{
	private final int executions;
	private static Set<TimeInformation> timeInfos = new HashSet<>();
	
	public CommonPerformanceTest(int executions) {
		this.executions = executions;
	}
	
	class TimeInformation {
		long[] time = new long[executions];
		long[] gcTime = new long[executions];
		long[] cpuTime = new long[executions];
		
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
		
		for(int i = -1; i < executions; i++) {
			
			if(i != -1) System.out.print("  run " + (i+1) + " of " + executions + " started...");
			
			SkillFile sf = SkillFile.open(filepath);
	        sf.changePath(tmpFile(filename));

	        if(i == -1) ti.totalObjects = countObjects(sf);
	        
	        Set<CollectionRoot> roots = new HashSet<>();
	        roots.add(new CollectionRoot("imlgraph"));

	        long startCPU = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();

	        long startGcTime = getGarbageCollectionTime();
	        
	        long start = System.currentTimeMillis();
	        try {
	        	GarbageCollector.run(sf, roots, false, false, false);
	        } catch (OutOfMemoryError e) {
	        	System.out.print("...Out of Memory...");
	        }
	        long end = System.currentTimeMillis();
	        
	        long endGcTime = getGarbageCollectionTime();

	        long endCPU = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
	        
	        if(i != -1) {
		        ti.time[i] = end - start;
		        ti.gcTime[i] = endGcTime - startGcTime;
		        ti.cpuTime[i] = Math.round((endCPU - startCPU) / 1E6);
		        System.out.println("finished (" + ti.time[i] + " ms)");
	        }
	         
	        System.gc();
		}
	}

	private int countObjects(SkillFile sf) {
		int objCount = 0;
		for(Access<? extends SkillObject> t : sf.allTypes()) {
			if(t.superName() == null) objCount += t.size();
		}
		return objCount;
	}
	
	private static long getGarbageCollectionTime() {
	    long collectionTime = 0;
	    for (GarbageCollectorMXBean garbageCollectorMXBean : ManagementFactory.getGarbageCollectorMXBeans()) {
	        collectionTime += garbageCollectorMXBean.getCollectionTime();
	    }
	    return collectionTime;
	}
	
	@AfterAll
	static void performanceAnalysis() throws Exception {
		String filename = new SimpleDateFormat("'output/performanceTest-'yyyy-MM-dd-HH-mm-ss'.csv'").format(new Date());
		PrintWriter pw = new PrintWriter(new File(filename));
        StringBuilder sb = new StringBuilder();
        sb.append("filename,object count,time(ms),gc time(ms),cpu time(ms)\n");
	
		for(TimeInformation ti : timeInfos) {
			System.out.println(ti.filename);
			System.out.println("  Object count: " + ti.totalObjects);
			
			double average = 0;
			for (int i = 0; i < ti.time.length; i++) {
				sb.append(ti.filename).append(",").append(ti.totalObjects).append(",").append(ti.time[i])
					.append(",").append(ti.gcTime[i]).append(",").append(ti.cpuTime[i]).append("\n");
				average += ti.time[i];
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
