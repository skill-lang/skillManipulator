package de.ust.skill.manipulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import de.ust.skill.common.java.api.Access;
import de.ust.skill.common.java.api.SkillException;
import de.ust.skill.common.java.internal.FieldDeclaration;
import de.ust.skill.common.java.internal.FieldIterator;
import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.common.java.internal.StaticDataIterator;
import de.ust.skill.common.java.internal.StaticFieldIterator;
import de.ust.skill.common.java.internal.StoragePool;
import de.ust.skill.common.java.internal.TypeHierarchyIterator;
import de.ust.skill.ir.ParseException;
import de.ust.skill.ir.TypeContext;
import de.ust.skill.ir.UserType;
import de.ust.skill.parser.Parser;

public class Main {
	
	
	public static <T> void main(String[] args) {
		double a = Double.NEGATIVE_INFINITY;
		
		if(Long.MAX_VALUE < a) {
			System.out.println(a + " > " + Long.MAX_VALUE);
		}

//		File folder = new File("/media/olibroe/DATA/Ubuntu_Data/imlfiles/");
//		HashSet<GarbageCollector.CollectionRoot> roots = new HashSet<>();
//		roots.add(new CollectionRoot("imlgraph"));
//		roots.add(new CollectionRoot("metainformation"));
//		for(File f : folder.listFiles()) {
//			SkillFile sf;
//			try {
//				sf = SkillFile.open(f.getAbsolutePath());
//				System.out.println(f.getName());
//				GarbageCollector.run(sf, roots, false, true, false);
//				System.out.println();
//			} catch (SkillException | IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//		}
		
		
//		HashMap<String, Integer> valueMap = new HashMap<>();
//		File folder = new File("/media/olibroe/DATA/Ubuntu_Data/imlfiles2/");
//		try {
//			for(File f : folder.listFiles()) {
//				SkillFile sf = SkillFile.open(f.getAbsolutePath());
//				SkillState state = (SkillState) sf;
//				
//				StoragePool<?,?> value = state.pool("value");
//				valueMap.put(f.getName(), value.size());
//				System.out.println(state.getTypes().size());
//			}
//		    File inputF = new File("/media/olibroe/DATA/Masterarbeit/Data/performanceTest-removeValue_smallTypesystem.csv");
//		    InputStream inputFS = new FileInputStream(inputF);
//		    BufferedReader br = new BufferedReader(new InputStreamReader(inputFS));
//		    
//		    PrintWriter pw = new PrintWriter(new File("/media/olibroe/DATA/Masterarbeit/Data/performanceTest-removeValue_smallTypesystem2.csv"));
//		    String line;
//		    while((line = br.readLine()) != null) {
//		    	String[] parts = line.split(",");
//		    	Integer oc = valueMap.get(parts[0]);
//		    	if(oc != null) {
//		    		pw.write(line + "," + oc + "\n");
//		    	} else {
//		    		pw.write(line + "," + "value object count" + "\n");
//		    	}
//		    	System.out.println(parts[0]);
//		    }
//		    br.close();
//		    pw.close();
//		} catch (SkillException | IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
//	    try {	    	
//	    	SkillFile sf = SkillFile.open("/tmp/bash.iml.sf");
//	    	SkillFile sf = SkillFile.open("src/test/resources/performance/gnugo.iml.sf");
//	    	SkillFile sf = SkillFile.open("src/test/resources/simpleIml/simple.iml.sf");
//	    	SkillFile sf = SkillFile.open("/media/olibroe/DATA/Ubuntu_Data/gitlab/skill/testsuites/java/test/age.sf", Mode.Read);
	    	//SkillFile sf = SkillFile.open("/media/olibroe/DATA/Ubuntu_Data/gitlab/skill/testsuites/java/src/test/resources/date-example-with-empty-age-pool.sf", Mode.Read);
	    	
//	    	Path path = new File("/tmp/test.sf").toPath();
//	    	sf.changePath(path);
	    	
//	    	Set<CollectionRoot> roots = new HashSet<>();
//			roots.add(new CollectionRoot("imlgraph"));		
			
	    	//System.out.println(sf.Strings().size());
//			System.out.println("Press enter");
//			Scanner scanner = new Scanner(System.in);
//			scanner.next();
//			scanner.close();
	    	
			//TypeUtils.deleteType(sf, "age");
	    	
//			GarbageCollector.run(sf, roots, false, true, false);
//			
//			sf.close();
			
			
//			TypeContext tc = Parser.process(new File("src/test/resources/EmptyTypePool/Reference/specification.skill"), false, false, false, false);
//	    	TypeContext tc = Parser.process(new File("src/test/resources/age/age.skill"), false, false, false, false);
//			tc = tc.removeSpecialDeclarations();
			
//			for(UserType t : tc.getUsertypes()) {
//				System.out.println(t.prettyPrint());
//			}
			
//			SkillFile sf = SkillFile.open("src/test/resources/EmptyTypePool/Reference/reference.sf");
//			SkillFile sf = SkillFile.open("src/test/resources/age/age-example.sf");
//			
//			SpecificationMapper.map(tc, sf, File.createTempFile("test", ".sf").toPath());
			
//	    } catch (SkillException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (ParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

	}

}
