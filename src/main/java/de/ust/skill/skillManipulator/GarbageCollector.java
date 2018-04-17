package de.ust.skill.skillManipulator;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.ust.skill.common.java.api.Access;
import de.ust.skill.common.java.api.FieldDeclaration;
import de.ust.skill.common.java.api.FieldType;
import de.ust.skill.common.java.internal.FieldIterator;
import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.common.java.internal.fieldTypes.MapType;
import de.ust.skill.common.java.internal.fieldTypes.SingleArgumentType;

/**
 * state of a garbage collection run
 * 
 * usage: new GCRun(SkillFile sf, Set<String> roots, Set<Integer> rootIds, boolean printStatistics, boolean printProgress)
 * Do not create a variable for this. 
 */
public class GarbageCollector {
	private SkillFile sf;
	
	private Set<String> rootTypes;
	// TODO data structure with skillID + skillname
	private Set<Integer> rootIds;
	
	private Set<SkillObject> seen;
	private Set<String> seenStrings;
	private List<SkillObject> todo;
				
	private Map<String, Access<? extends SkillObject>> types;
	
	private long totalObjects = 0;
	
	private boolean printStatistics;
	private boolean printProgress;
	
	/**
	 * Represents main method of the garbage collection.
	 * 
	 * The garbage collection starts with marking the root nodes and then
	 * marks recursive all nodes reachable from the root nodes. The unmarked
	 * nodes are deleted.
	 * 
	 * @param sf - SkillFile to process
	 * @param rootTypes - String set of root types
	 * @param rootIds - Integer set of root ids
	 * @param printStatistics - Do you want statistics printed out ?
	 * @param printProgress - Do you want to get progress printed out ?
	 */
	public static void run(SkillFile sf, Set<String> rootTypes, Set<Integer> rootIds, boolean printStatistics, boolean printProgress) {
		long start = System.currentTimeMillis();
		System.out.println("Starting garbage collection");

		GarbageCollector gc = new GarbageCollector(sf, rootTypes, rootIds, printStatistics, printProgress);
        
        // 1.Phase: add all root nodes
		gc.addRoots();

		// 2.Phase: add all nodes reachable from roots to "seen" Sets
		gc.processNodes();

		// 3.Phase:
		gc.removeDeadNodes();

		gc = null;
		
		System.out.println("done. Time: " + (System.currentTimeMillis() - start));
	}
	

	private GarbageCollector(SkillFile sf, Set<String> rootTypes, Set<Integer> rootIds, boolean printStatistics, boolean printProgress) {
		this.sf = sf;
		
		this.rootTypes = rootTypes != null ? rootTypes : new HashSet<>();
		// TODO data structure has to be exchanged, just IDs are not unique (type + id is unique)
		this.rootIds = rootIds != null ? rootIds : new HashSet<>();
		
		this.seen = new HashSet<>();
		this.seenStrings = new HashSet<>();
		
		// important to choose linked list here, because of removing first element in loop later
		this.todo = new LinkedList<>();
		
		// create types map of the form: typename -> Access<? extends SkillObject>
		this.types = new HashMap<>();
        for (Access<?> t : sf.allTypes()) {
            this.types.put(t.name(), t);
            if(t.superName() == null) {
            	this.totalObjects += t.size();
            }
        }	
        
        this.printStatistics = printStatistics;
        this.printProgress = printProgress;
        
	}
	
	private void addRoots() {
		// loop over all nodes and add them to seen list if they are root elements
		for(Access<?> t : sf.allTypes()) {
			for(SkillObject o : t) {
				if(!o.isDeleted()) {
					if(rootTypes.contains(t.name()) || rootIds.contains(o.getSkillID())) {
						seen.add(o);
					}
				}
			}
		}
		
		// first todo elements are the roots
		todo.addAll(seen);
		
		if (printStatistics) {
		      System.out.println("  total objects: " + totalObjects);
		      System.out.println("  root objects: " + seen.size());
		}
	}
	
	private void processNodes() {
		if (printProgress) { 
			System.out.println("collecting");
		}

		long processedObjects = 0;
		while (!todo.isEmpty()) {
			SkillObject node = todo.get(0);
			todo.remove(node);
			
			if(!node.isDeleted()) {
				if (printProgress) {
					processedObjects += 1;
				    if (totalObjects > 10 && (0 == processedObjects % (totalObjects / 10))) {
				    	System.out.print(".");
				    }
				}
				
				// visit all fields of that node
				Access<?> t = types.get(node.skillName());
				
				seenStrings.add(t.name());
				
				FieldIterator fit = t.allFields();
				while(fit.hasNext()) {
					FieldDeclaration<?> f = fit.next();
					if(!ignoreType(f.type())) {
						seenStrings.add(f.name());
						seenStrings.add(f.type().toString());
						Object o = f.get(node);					
						if(o != null) {
							processObject(f.type(), o);
						}
					}
				}
			}
		}
		
		if (printProgress) {
			System.out.println("done processing " + processedObjects + " nodes");
		}
	}
	
	private void processObject(FieldType<?> t, Object o) {
		int id = t.typeID();
		if (14 == id) {
			// string
			seenStrings.add((String) o);
		} else if (15 <= id && id <= 19) {
			// linear collection
			FieldType<?> bt = ((SingleArgumentType<?, ?>)t).groundType;
			for(Object i : (Iterable<?>)o) {
				processObject(bt, i);
			}
		} else if (20 == id) {
			// map
			MapType<?, ?> mt = (MapType<?, ?>)t; 
			boolean followKey = !ignoreType(mt.keyType);
			boolean followVal = !ignoreType(mt.valueType);
			for(Map.Entry<?, ?> i : ((HashMap<?, ?>) o).entrySet()) {
				if(followKey) processObject(mt.keyType, i.getKey());
				if(followVal) processObject(mt.valueType, i.getValue());
			}
		} else {
			// ref
			SkillObject ref = (SkillObject)o;
			if (ref != null && !seen.contains(ref)) {
				seen.add(ref);
				todo.add(ref);
			}
		}
	}
	
	private void removeDeadNodes() {
		if (printStatistics) {
			System.out.println("  reachable: " + seen.size());
	    }

		for(Access<?> t : sf.allTypes()) {
			if(t.superName() == null) {
				for(SkillObject o : t) {
					if(!seen.contains(o)) {
						if(printProgress) {
							System.out.println("delete: " + o);
						}
						sf.delete(o);
					}
				}
			}
		}
		
		Set<String> toRemove = new HashSet<>();
		for(String s : sf.Strings()) {
			if(!seenStrings.contains(s)) {
				if(printProgress) {
					System.out.println("delete string: " + s);
				}
				toRemove.add(s);
			}
		}
		sf.Strings().removeAll(toRemove);
	}

	private boolean ignoreType(FieldType<?> t) {
		int id = t.typeID();
		if (id < 14 && id != 5) {
			return true;
		} else if (15 <= id && id <= 19) {
			return ignoreType(((SingleArgumentType<?, ?>)t).groundType);
		} else if (20 == id) {
			return ignoreType(((MapType<?, ?>)t).keyType) && ignoreType(((MapType<?, ?>)t).valueType);
		} else {
			return false;
		}
	}

}
