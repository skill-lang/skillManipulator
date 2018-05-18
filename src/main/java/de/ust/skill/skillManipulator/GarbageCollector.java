package de.ust.skill.skillManipulator;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.ust.skill.common.java.api.FieldType;
import de.ust.skill.common.java.internal.FieldDeclaration;
import de.ust.skill.common.java.internal.FieldIterator;
import de.ust.skill.common.java.internal.InterfacePool;
import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.common.java.internal.StoragePool;
import de.ust.skill.common.java.internal.fieldTypes.MapType;
import de.ust.skill.common.java.internal.fieldTypes.SingleArgumentType;

/**
 * state of a garbage collection run
 * 
 * @author olibroe
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
	
	// internal implementation of SkillFile is used here to have direct access to types and strings
	private final SkillState state;
	
	private int totalObjects = 0;
	
	private final boolean printStatistics;
	private final boolean printProgress;

	// the mark status of all objects is saved in objReachable
	// to calculate the index of objReachable the skillID is used
	// the skillID is unique for every root type (super type is null), so we need a type offset for 
	// all types that are not subtypes of the first root type
	// calculation of index is: typeOffsets[t.typeID-32] + obj.getSkillID() - 1
	private final int[] typeOffsets;
	private final BitSet objReachable; 
	
	// if the flag is set all fields that represent collections are not erased if the types have no objects
	// the keepTypes data structure stores then the types we need for the collections to exist
	private boolean keepCollectionFields;
	private Set<StoragePool<?, ?>> keepTypes = new HashSet<>();
	
	// fast working data structure to add and remove elements
	private Deque<SkillObject> workingQueue;
	
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
		
		// create garbage collection object
		GarbageCollector gc = new GarbageCollector(sf, keepCollectionFields, printStatistics, printProgress);

		if(printStatistics || printProgress) System.out.println("Starting garbage collection");
		
		int rootObjects = 0;

		// loop over roots and process them
		if(roots != null) {
			for(CollectionRoot root : roots) {
				StoragePool<?, ?> s = gc.state.pool(root.type);
				if(s != null) {
					if(root.id == CollectionRoot.ALL_IDS) {
						for(SkillObject o : s) {	
							gc.workingQueue.push(o);
							gc.processSkillObject();
							rootObjects++;
						}
					} else {
						SkillObject o = s.getByID(root.id);
						if(o != null) {
							gc.workingQueue.push(o);
							gc.processSkillObject();
							rootObjects++;
						}
					}
				}
			}
		}

		if (printStatistics) {
		      System.out.println("  total objects: " + gc.totalObjects);
		      System.out.println("  root objects: " + rootObjects);
		      System.out.println("Collecting done: " + (System.currentTimeMillis() - start));
		}

		// remove dead objects, that are not reachable from the root objects
		gc.removeDeadObjects();		

		// remove types and fields that are not used
		gc.removeDeadTypesAndFields();
		
		if(printStatistics || printProgress) System.out.println("done. Time: " + (System.currentTimeMillis() - start));
	}

	private GarbageCollector(SkillFile sf, boolean keepCollectionFields, boolean printStatistics, boolean printProgress) {
		this.state = (SkillState) sf;
		
		// types have to be in type order
		this.typeOffsets = new int[this.state.getTypes().size()];
        for (StoragePool<?,?> t : this.state.getTypes()) {
            if(t.superName() == null) {
            	this.typeOffsets[t.typeID-32] = this.totalObjects;
            	this.totalObjects += t.size();
            } else {
            	this.typeOffsets[t.typeID-32] = this.typeOffsets[t.typeID-32-1];
            }
        }	
        this.objReachable = new BitSet(totalObjects);
          
        this.workingQueue = new ArrayDeque<>(10000);
        
        this.keepCollectionFields = keepCollectionFields;
        
        this.printStatistics = printStatistics;
        this.printProgress = printProgress;
        
	}
	
	/**
	 * Takes Object from the queue, marks it and processes its fields with processFields method.
	 * If there are object references in the fields, they are added to the queue.
	 * 
	 * Shortly: Take starting object and run through all childs to mark them
	 */
	private void processSkillObject() {
		while(!workingQueue.isEmpty()) {
			// get next object
			SkillObject obj = workingQueue.pop();

			// get pool
			StoragePool<?, ?> type = state.pool(obj.skillName());

			//mark node
			objReachable.set(typeOffsets[type.typeID-32] + obj.getSkillID() - 1);

			// visit all fields of that node
			FieldIterator fit = type.allFields();
			FieldDeclaration<?,?> f;
			Object o;

			while(fit.hasNext()) {
				f = fit.next();
				if(!ignoreType(f.type())) {
					o = f.get(obj);				
					if(o != null) {
						processField(f.type(), o);
					}
				}
			}
		}
	}
	
	/**
	 * Process field of given field type and value.
	 * If the field is a reference to another SkillObject, we have to process the object.
	 * References can also be in collection types.
	 *
	 * @param t - field type
	 * @param o - value of the field
	 */
	private void processField(FieldType<?> t, Object o) {
		switch(t.typeID()) {
		case 15:
		case 16:
		case 17:
		case 18:
		case 19:
			// linear collection
			FieldType<?> bt = ((SingleArgumentType<?, ?>)t).groundType;
			for(Object i : (Iterable<?>)o) {
				processField(bt, i);
			}
			break;
		case 20:
			// map
			MapType<?, ?> mt = (MapType<?, ?>)t; 
			boolean followKey = !ignoreType(mt.keyType);
			boolean followVal = !ignoreType(mt.valueType);
			for(Map.Entry<?, ?> i : ((HashMap<?, ?>) o).entrySet()) {
				if(followKey) processField(mt.keyType, i.getKey());
				if(followVal) processField(mt.valueType, i.getValue());
			}
			break;
		default:
			// ref
			SkillObject ref = (SkillObject)o;
			if(ref == null || ref.isDeleted()) {
				return;
			}
			if (!objReachable.get(typeOffsets[t.typeID()-32] + ref.getSkillID() - 1)) {
				workingQueue.push(ref);
			}	
		}
	}
	
	/**
	 * Remove all dead objects (objects that are not marked in the previous step)
	 * Since the root types know all objects of themselves and their subtypes it is 
	 * sufficient to loop over their objects (superPool == null)
	 */
	private void removeDeadObjects() {
		for(StoragePool<?,?> t : state.getTypes()) {
			if(t.superPool == null)
				for(SkillObject o : t) 
					if(!o.isDeleted() && !objReachable.get(typeOffsets[t.typeID-32] + o.getSkillID() - 1)) {
						if(printProgress) System.out.println("delete: " + o);
						state.delete(o);
					}
		}
		
		if (printStatistics) System.out.println("  reachable: " + objReachable.cardinality());
	}

	/**
	 * Remove Dead Types and Fields
	 * Dead means:
	 * - for a field: it is not possible to store information in this field
	 *   (for example: a reference to a type and there are no objects of this type existing)
	 * - for a type: there are no objects of this type
	 * When removing types or fields, one has to reorder them and refresh their IDs.
	 */
	private void removeDeadTypesAndFields() {
		// get list of all types
		ArrayList<StoragePool<?, ?>> types = state.getTypes();

		// fix types to have faster size operations
		StoragePool.fixed(types);
		
		// step through the fields of all types and remove unneeded fields
		for (StoragePool<?, ?>  type : types) {
			ArrayList<FieldDeclaration<?, ?>> irrFields = new ArrayList<>();

			for (FieldDeclaration<?, ?> field : type.dataFields) {
				if(!keepField(field.type())) irrFields.add(field);
			}

			if (irrFields.size() > 0) {
				type.dataFields.removeAll(irrFields);
				FieldUtils.reorderFields(type);
			}
		}
		
		// keep type if there are objects of this type or we need the type for a collection
		ArrayList<StoragePool<?, ?>> irrtypes = new ArrayList<>();
		for (StoragePool<?, ?> pool : types) {
			if(pool.size() == 0 && !keepTypes.contains(pool)) irrtypes.add(pool);
		}
		
		// if there are types we want to delete, we have to reorder the types
		if (irrtypes.size() > 0) {
			types.removeAll(irrtypes);
			TypeUtils.reorderTypes(state);
		}		
		
		// unfix types
		StoragePool.unfix(types);
		
		// renew StringPool
		state.Strings().clear();
		state.collectStrings();
	}
	
	/**
	 * Returns true if the given field type is needed.
	 *
	 * @param t - type of field
	 * @return true if needed, otherwise false
	 */
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
		
		// should not be reached
		return false;
	}

	/**
	 * Returns true if we can ignore the given field type.
	 * This is the case for all non-user types, because we only want to remove dead SkillObjects.
	 *
	 * @param t - type of field
	 * @return true if type can be ignored, otherwise false
	 */
	private boolean ignoreType(FieldType<?> t) {
		int id = t.typeID();
		// simple data types
		if (id <= 14) {
			return true;
		// linear collections
		} else if (15 <= id && id <= 19) {
			return ignoreType(((SingleArgumentType<?, ?>)t).groundType);
		// maps
		} else if (20 == id) {
			return ignoreType(((MapType<?, ?>)t).keyType) && ignoreType(((MapType<?, ?>)t).valueType);
		// user types
		} else {
			return false;
		}
	}
}
