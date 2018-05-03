package de.ust.skill.skillManipulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import de.ust.skill.common.java.api.FieldType;
import de.ust.skill.common.java.internal.FieldDeclaration;
import de.ust.skill.common.java.internal.FieldIterator;
import de.ust.skill.common.java.internal.InterfacePool;
import de.ust.skill.common.java.internal.LazyField;
import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.common.java.internal.StoragePool;
import de.ust.skill.common.java.internal.fieldTypes.MapType;
import de.ust.skill.common.java.internal.fieldTypes.SingleArgumentType;

/**
 * state of a garbage collection run
 * 
 */
public class GarbageCollector {
	
	/**
	 * The Garbage Collector uses instances of this class as root elements for the
	 * garbage collection. There are two constructors to instantiate:
	 * - CollectionRoot(String type): use all objects of given type as roots
	 * - CollectionRoot(String type, int id): use only object of this type and id as root
	 * 	 The given ID can not be zero or negative, in this case the constructor behaves like
	 *   the constructor used for whole types.
	 * 
	 * If there is no object found that equals the specification by CollectionRoot, the root is
	 * simply not used. 
	 * 
	 * @author olibroe
	 *
	 */
	public static class CollectionRoot {
		private static final int ALL_IDS = 0;
		
		private String type;
		private int id;
		
		public CollectionRoot(String type) {
			this.type = type;
			this.id = ALL_IDS;
		}
		
		public CollectionRoot(String type, int id) {
			this.type = type;
			if(id <= 0)
				this.id = ALL_IDS;
			else
				this.id = id;
		}
	}
	
	// TODO probably not useful here
	static ThreadPoolExecutor pool = new ThreadPoolExecutor(Runtime.getRuntime().availableProcessors(),
            Runtime.getRuntime().availableProcessors(), 0L, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    final Thread t = new Thread(r);
                    t.setDaemon(true);
                    t.setName("SkillGCThread");
                    return t;
                }
            });
	
	// internal implementation of SkillFile is used here to have direct access to types and strings
	private SkillState state;
	
	private int totalObjects = 0;
	
	private boolean printStatistics;
	private boolean printProgress;

	// the mark status of all objects is saved in objReachable
	// to calculate the index of objReachable the skillID is used
	// the skillID is unique for every root type (super type is null), so we need a type offset for 
	// all types that are not subtypes of the first root type
	// calculation of index is: typeOffsets[t.typeID-32] + obj.getSkillID() - 1
	private int[] typeOffsets;
	private boolean[] objReachable; 
	
	private boolean keepCollectionFields = false;
	private Set<StoragePool<?, ?>> keepTypes;
	
	/**
	 * Represents main method of the garbage collection.
	 * 
	 * The garbage collection starts with marking the root nodes and then
	 * marks recursive all nodes reachable from the root nodes. The unmarked
	 * nodes are deleted.
	 * 
	 * @param sf - SkillFile to process
	 * @param roots - represents the roots of the garbage collection
	 * @param keepCollectionFields - keep all collection fields and the types used in them
	 * @param printStatistics - Do you want statistics printed out ?
	 * @param printProgress - Do you want to get progress printed out ?
	 */
	public static void run(SkillFile sf, Set<CollectionRoot> roots, boolean keepCollectionFields, boolean printStatistics, boolean printProgress) {
		long start = System.currentTimeMillis();
		
		if(printProgress) System.out.println("Starting garbage collection");
		
		GarbageCollector gc = new GarbageCollector(sf, keepCollectionFields, printStatistics, printProgress);
		
		int rootObjects = 0;
		
//		for(int i = 0; i < pool.getMaximumPoolSize(); i++) {
//			pool.execute(gc.new ProcessObject());
//		}

		if(roots != null) {
			for(CollectionRoot root : roots) {
				StoragePool<?, ?> s = gc.state.pool(root.type);
				if(s != null) {
					if(root.id == CollectionRoot.ALL_IDS) {
						for(SkillObject o : s) {

//							try {
//								gc.queue.put(o);
//								// TODO better solution
//								Thread.sleep(5);
//								gc.threadsIdle.acquire(pool.getMaximumPoolSize());
//							} catch (InterruptedException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
							
							
							
							gc.processSkillObject(o);
							rootObjects++;
						}
					} else {
						SkillObject o = s.getByID(root.id);
						if(o != null) gc.processSkillObject(o);
					}
				}
			}
		}
		
		
		if (printStatistics) {
		      System.out.println("  total objects: " + gc.totalObjects);
		      System.out.println("  root objects: " + rootObjects);
		}

		System.out.println("Collecting done: " + (System.currentTimeMillis() - start));
		
		gc.removeDeadObjects();		
		
		System.out.println("Done removing dead nodes: " + (System.currentTimeMillis() - start));
		
		gc.removeDeadTypes();
		gc.state.Strings().clear();
		
		System.out.println("Done removing dead types and fields: " + (System.currentTimeMillis() - start));
		
		gc = null;
		
		if(printProgress) System.out.println("done. Time: " + (System.currentTimeMillis() - start));
	}

	private GarbageCollector(SkillFile sf, boolean keepCollectionFields, boolean printStatistics, boolean printProgress) {
		this.state = (SkillState) sf;
		
		this.typeOffsets = new int[this.state.getTypes().size()];
        for (StoragePool<?,?> t : this.state.getTypes()) {
            if(t.superName() == null) {
            	this.typeOffsets[t.typeID-32] = this.totalObjects;
            	this.totalObjects += t.size();
            } else {
            	this.typeOffsets[t.typeID-32] = this.typeOffsets[t.typeID-32-1];
            }
        }	
        this.objReachable = new boolean[totalObjects];
          
        this.keepCollectionFields = keepCollectionFields;
        this.keepTypes = new HashSet<>();
        
        this.printStatistics = printStatistics;
        this.printProgress = printProgress;
        
	}
	
	private void processSkillObject(SkillObject obj) {
		// get pool
		StoragePool<?, ?> t = state.pool(obj.skillName());

		//mark node
		objReachable[typeOffsets[t.typeID-32] + obj.getSkillID() - 1] = true;

		// visit all fields of that node
		FieldIterator fit = t.allFields();

		while(fit.hasNext()) {
			FieldDeclaration<?,?> f = fit.next();
			if(!ignoreType(f.type())) {
				Object o = f.get(obj);				
				if(o != null) {
					processField(f.type(), o);
				}
			}

		}
	}
	
	private void processField(FieldType<?> t, Object o) {
		int id = t.typeID();
		if (15 <= id && id <= 19) {
			// linear collection
			FieldType<?> bt = ((SingleArgumentType<?, ?>)t).groundType;
			for(Object i : (Iterable<?>)o) {
				processField(bt, i);
			}
		} else if (20 == id) {
			// map
			MapType<?, ?> mt = (MapType<?, ?>)t; 
			boolean followKey = !ignoreType(mt.keyType);
			boolean followVal = !ignoreType(mt.valueType);
			for(Map.Entry<?, ?> i : ((HashMap<?, ?>) o).entrySet()) {
				if(followKey) processField(mt.keyType, i.getKey());
				if(followVal) processField(mt.valueType, i.getValue());
			}
		} else {
			// ref
			SkillObject ref = (SkillObject)o;
			if(ref == null || ref.isDeleted()) {
				return;
			}
			if (!objReachable[typeOffsets[id-32] + ref.getSkillID() - 1]) {
				processSkillObject(ref);
			}
		}
	}
	
	private void removeDeadObjects() {
		int reachable = objReachable.length;
		for(StoragePool<?,?> t : state.getTypes()) {
			if(t.superName() == null) 
				for(SkillObject o : t) 
					if(!o.isDeleted() && !objReachable[typeOffsets[t.typeID-32] + o.getSkillID() - 1]) {
						if(printProgress) System.out.println("delete: " + o);
						reachable--;
						state.delete(o);
					}
		}
		if (printStatistics) {
			System.out.println("  reachable: " + reachable);
		}
	}


	private void removeDeadTypes() {		
		ArrayList<StoragePool<?, ?>> types = state.getTypes();
		
		ArrayList<StoragePool<?, ?>> rtypes = new ArrayList<>();
		ArrayList<StoragePool<?, ?>> irrtypes = new ArrayList<>();

		StoragePool.fixed(types);
		
		for (StoragePool<?, ?>  s : types) {
			ArrayList<FieldDeclaration<?, ?>> rFields = new ArrayList<>();
			ArrayList<FieldDeclaration<?, ?>> irrFields = new ArrayList<>();

			for (FieldDeclaration<?, ?> f : s.dataFields) {
				if (keepField(f.type())) {
					rFields.add(f);
				} else {
					irrFields.add(f);
				}
			}

			if (irrFields.size() > 0) {
				s.dataFields.clear();
				int nextID = 1;
				for (FieldDeclaration<?, ?> f : rFields) {
					f.index = nextID;
					s.addField(f);
					++nextID;
				}
			}
		}
		
		
		for (StoragePool<?, ?> pool : types) {
			if (pool.size() > 0)
				rtypes.add(pool);
			else
				irrtypes.add(pool);
		}
		
		for(StoragePool<?, ?> s : keepTypes) { 
			if(!rtypes.contains(s)) rtypes.add(s);
		}
		irrtypes.removeAll(keepTypes);
		
		if (irrtypes.size() > 0) {
			types.clear();
			int nextID = 32;
			for (StoragePool<?, ?>  s : rtypes) {
				s.typeID = nextID;
				++nextID;
				types.add(s);
				s.setNextPool(null);
			}	
			
		
			StoragePool.establishNextPools(types);
		}		
		
		StoragePool.unfix(types);
	}
	
	private boolean keepField(FieldType<?> t) {
		// basic types
		if(t.typeID() < 15) {
			return true;
		}
		// user types
		if(t.typeID() >= 32) {
			if(t instanceof StoragePool<?, ?>) {
				return ((StoragePool<?,?>) t).size() > 0;
			}
			if(t instanceof InterfacePool<?, ?>) {
				return ((StoragePool<?,?>) t).superPool.size() > 0;
			}
			return false;
		}
		// linear collections
		if(15 <= t.typeID() && t.typeID() <= 19) {
			if(keepCollectionFields && (((SingleArgumentType<?, ?>)t).groundType).typeID >= 32) {
				keepTypes.add((StoragePool<?, ?>)((SingleArgumentType<?, ?>)t).groundType);
				return true;
			}
			return keepField(((SingleArgumentType<?, ?>)t).groundType);
		}
		// map
		if(t.typeID() == 20) {
			if(keepCollectionFields) {
				if(((MapType<?, ?>)t).keyType.typeID >= 32) 
					keepTypes.add((StoragePool<?, ?>)((MapType<?, ?>)t).keyType);
				else
					keepField(((MapType<?, ?>)t).keyType);
				
				if(((MapType<?, ?>)t).valueType.typeID >= 32) 
					keepTypes.add((StoragePool<?, ?>)((MapType<?, ?>)t).valueType);
				else
					keepField(((MapType<?, ?>)t).valueType);
				
				return true;
			}
			return (keepField(((MapType<?, ?>)t).keyType) && keepField(((MapType<?, ?>)t).valueType));
		}
		
		return false;
	}

	private boolean ignoreType(FieldType<?> t) {
		int id = t.typeID();
		if (id <= 14 && id != 5) {
			return true;
		} else if (15 <= id && id <= 19) {
			return ignoreType(((SingleArgumentType<?, ?>)t).groundType);
		} else if (20 == id) {
			return ignoreType(((MapType<?, ?>)t).keyType) && ignoreType(((MapType<?, ?>)t).valueType);
		} else {
			return false;
		}
	}
	
	private BlockingQueue<SkillObject> queue = new LinkedBlockingQueue<>(pool.getMaximumPoolSize()/2);
	private Semaphore threadsIdle = new Semaphore(pool.getMaximumPoolSize());
	
	class ProcessObject implements Runnable {
//		private SkillObject obj;
//		
//		ProcessObject(SkillObject obj) {
//			this.obj = obj;
//			jobCount.incrementAndGet();
//		}
		
		@Override
		public void run() {
			SkillObject take;
			while(true) {
				try {
					take = queue.take();
					threadsIdle.acquire();
					processSkillObject(take);
					threadsIdle.release();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
		private void processSkillObject(SkillObject obj) {
			// get pool
			StoragePool<?, ?> t = state.pool(obj.skillName());
			
			if(objReachable[typeOffsets[t.typeID-32] + obj.getSkillID() - 1]) return;
			
			//mark node
			objReachable[typeOffsets[t.typeID-32] + obj.getSkillID() - 1] = true;
			
			// visit all fields of that node
			FieldIterator fit = t.allFields();
			
			while(fit.hasNext()) {
				FieldDeclaration<?,?> f = fit.next();
				if(!ignoreType(f.type())) {
					Object o;
					if(f instanceof LazyField<?, ?>) {
						synchronized(state) {
							o = f.get(obj);		
						}
					} else {
						o = f.get(obj);	
					}
					if(o != null) {
						processField(f.type(), o);
					}
				}

			}
		}
		
		private void processField(FieldType<?> t, Object o) {
			int id = t.typeID();
			if (15 <= id && id <= 19) {
				// linear collection
				FieldType<?> bt = ((SingleArgumentType<?, ?>)t).groundType;
				for(Object i : (Iterable<?>)o) {
					processField(bt, i);
				}
			} else if (20 == id) {
				// map
				MapType<?, ?> mt = (MapType<?, ?>)t; 
				boolean followKey = !ignoreType(mt.keyType);
				boolean followVal = !ignoreType(mt.valueType);
				for(Map.Entry<?, ?> i : ((HashMap<?, ?>) o).entrySet()) {
					if(followKey) processField(mt.keyType, i.getKey());
					if(followVal) processField(mt.valueType, i.getValue());
				}
			} else {
				// ref
				SkillObject ref = (SkillObject)o;
				if (ref != null && !objReachable[typeOffsets[id-32] + ref.getSkillID() - 1]) {
//					if(pool.getActiveCount() < 0) {
//						pool.execute(new ProcessObject(ref));
//					} else {
//						processSkillObject(ref);
//					}
					if(!queue.offer(ref)) {
						processSkillObject(ref);
					}
					//queue.add(ref);
				}
			}
		}
		
	}
}
