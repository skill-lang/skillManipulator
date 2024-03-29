package de.ust.skill.manipulator.gc;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.ust.skill.common.java.api.FieldType;
import de.ust.skill.common.java.internal.FieldDeclaration;
import de.ust.skill.common.java.internal.FieldIterator;
import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.common.java.internal.StoragePool;
import de.ust.skill.common.java.internal.fieldTypes.MapType;
import de.ust.skill.common.java.internal.fieldTypes.SingleArgumentType;
import de.ust.skill.manipulator.OutputPrinter;
import de.ust.skill.manipulator.internal.SkillFile;
import de.ust.skill.manipulator.internal.SkillState;
import de.ust.skill.manipulator.utils.FieldUtils;
import de.ust.skill.manipulator.utils.TypeUtils;

/**
 * GarbageCollector deletes all Objects that are not reachable from the root set.
 * Additionaly the type system and the corresponding strings are reduced to a minimum.
 * 
 * basic idea by Timm Felden, see https://github.com/skill-lang/skillGC
 * modified by Oliver Brösamle
 * 
 */
public class GarbageCollector {
	
	// internal implementation of SkillFile is used here to have direct access to types and strings
	private final SkillState state;
	
	// total object count
	private int totalObjects = 0;
	
	private final boolean printStatistics;
	private final boolean printProgress;
	
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
	 * recursively marks all nodes reachable from the root nodes. The unmarked
	 * nodes are deleted. This is known as Mark-Sweep algorithm.
	 * 
	 * @param sf - SkillFile to process
	 * @param roots - represents the roots of the garbage collection
	 * @param keepCollectionFields - keep all collection fields and the types used in them
	 * @param printStatistics - Do you want statistics printed out ?
	 * @param printProgress - Do you want to get progress printed out ?
	 */
	public static void run(SkillFile sf, Set<CollectionRoot> roots, boolean keepCollectionFields, 
			boolean printStatistics, boolean printProgress) {
		
		long start = System.currentTimeMillis();
		
		// create garbage collection state
		GarbageCollector gc = new GarbageCollector(sf, keepCollectionFields, printStatistics, printProgress);

		if(printStatistics || printProgress) OutputPrinter.println("Starting garbage collection");
		
		int rootObjects = 0;

		// loop over roots and process them
		if(roots != null) {
			for(CollectionRoot root : roots) {
				// get type of root, if type does not exist => skip root
				StoragePool<?, ?> s = gc.state.pool(root.getType());
				if(s != null) {
					// all objects of the type or just one
					if(root.getId() == CollectionRoot.ALL_IDS) {
						// all objects of type s are roots
						for(SkillObject o : s) {	
							gc.workingQueue.push(o);
							gc.processSkillObject();
							rootObjects++;
						}
					} else {
						// get root object, if it does not exist => skip root
						SkillObject o = s.getByID(root.getId());
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
			OutputPrinter.println("  total objects: " + gc.totalObjects);
			OutputPrinter.println("  root objects: " + rootObjects);
			OutputPrinter.println("Collecting done: " + (System.currentTimeMillis() - start));
		}

		// remove dead objects, that are not reachable from the root objects
		gc.removeDeadObjects();		

		// remove types and fields that are not used
		gc.removeDeadTypesAndFields();
		
		if(printStatistics || printProgress)
			OutputPrinter.println("done. Time: " + (System.currentTimeMillis() - start));
	}

	/**
	 * Create the internal state of the garbage collection run.
	 * 
	 * @param sf - Skillfile
	 * @param keepCollectionFields - Do we have to keep all collection fields ?
	 * @param printStatistics - Print Statistics ? 
	 * @param printProgress - Print Progress ? 
	 */
	private GarbageCollector(SkillFile sf, boolean keepCollectionFields, boolean printStatistics,
			boolean printProgress) {
		
		// get the internal representation of the skillfile
		this.state = (SkillState) sf;
		
		// count total objects
        for (StoragePool<?,?> t : this.state.getTypes()) {
            if(t.superName() == null) this.totalObjects += t.size();
        }	
        
        // allocate working queue
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

			// get pool to loop over fields
			StoragePool<?, ?> type = state.pool(obj.skillName());

			// mark node
			obj.marked = true;

			// visit all fields of that node
			FieldIterator fit = type.allFields();
			FieldDeclaration<?,?> f;
			Object o;

			while(fit.hasNext()) {
				f = fit.next();
				// if the field type can not store references, it is not interesting for us
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
		case 15: // fixed length array
		case 17: // variable length array
		case 18: // list
		case 19: // set
			// get base type and process objects
			FieldType<?> bt = ((SingleArgumentType<?, ?>)t).groundType;
			for(Object i : (Iterable<?>)o) {
				processField(bt, i);
			}
			break;
		case 20:
			// map
			// process key and value
			// the recursive call to processField is able to process nested maps 
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
			if(ref == null) return;
			if(ref.isDeleted()) return;
				
			if(!ref.marked) {
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
		int reachable = totalObjects;
		
		for(StoragePool<?,?> t : state.getTypes()) {
			// it is enough to loop over objects of basepools to iterate through all objects
			if(t.superPool == null)
				for(SkillObject o : t) 
					if(!o.isDeleted() && !o.marked) {
						// if object is not marked => delete and decrement reachable
						if(printProgress) OutputPrinter.println("delete: " + o);
						state.delete(o);
						--reachable;
					}
		}
		
		if (printStatistics) OutputPrinter.println("  reachable: " + reachable);
	}

	/**
	 * Remove Dead Types and Fields
	 * Dead means:
	 * - for a field: it is not possible to store information in this field
	 *   (for example: a reference to a type and there are no objects of this type existing)
	 * - for a type: there are no objects of this type
	 * When removing types or fields, one has to reorder them and refresh their IDs.
	 * 
	 * It is important to process the fields first, because if we need to keep a collection
	 * the basetypes also need to exist.
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
				FieldUtils.renewFieldIndices(type);
			}
		}
		
		// keep type if there are objects of this type or we need the type for a collection
		// if a type is needed, also its superpools are needed
		Set<StoragePool<?, ?>> irrtypes = new HashSet<>();
		for (StoragePool<?, ?> pool : types) {
			if(keepTypes.contains(pool)) {
				StoragePool<?,?> superPool = pool.superPool;
				while(superPool != null) {
					irrtypes.remove(superPool);
					superPool = superPool.superPool;
				}
			} else {
				if(pool.size() == 0) irrtypes.add(pool);
			}
		}
		
		// if there are types we want to delete, we have to reorder the types
		if (irrtypes.size() > 0) {
			types.removeAll(irrtypes);
			TypeUtils.renewTypeIDs(state);
		}		
		
		// unfix types
		StoragePool.unfix(types);
		
		// renew StringPool
		// note: clear is enough, strings are automatically collected before write operation
		state.Strings().clear();
	}
	
	/**
	 * Returns true if the given field type is needed.
	 *
	 * @param t - type of field
	 * @return true if needed, otherwise false
	 */
	private boolean keepField(FieldType<?> t) {
		// basic types
		if(t.typeID() < 15) return true;
		
		// user types
		if(t.typeID() >= 32) return ((StoragePool<?,?>) t).size() > 0;
		
		// linear collections (15 <= id <= 19)
		// note: less than 15 can never reach this point
		if(t.typeID() <= 19) {
			if(keepCollectionFields && (((SingleArgumentType<?, ?>)t).groundType).typeID >= 32) {
				keepTypes.add((StoragePool<?, ?>)((SingleArgumentType<?, ?>)t).groundType);
				return true;
			}
			return keepField(((SingleArgumentType<?, ?>)t).groundType);
		}
		
		// map
		// note: this point is only reachable with typeId = 20
		if(keepCollectionFields) {
			if(((MapType<?, ?>)t).keyType.typeID >= 32) 
				keepTypes.add((StoragePool<?, ?>)((MapType<?, ?>)t).keyType);

			if(((MapType<?, ?>)t).valueType.typeID >= 32) 
				keepTypes.add((StoragePool<?, ?>)((MapType<?, ?>)t).valueType);
			else // else case important for nested maps, return value can be ignored
				keepField(((MapType<?, ?>)t).valueType);

			return true;
		}
		return keepField(((MapType<?, ?>)t).keyType) && keepField(((MapType<?, ?>)t).valueType);
	}

	/**
	 * Returns true if we can ignore the given field type.
	 * This is the case for all non-user types, because we only want to remove dead SkillObjects.
	 * Note: The order of the if-conditions is important here, 
	 * because otherwise we also need to check the lower bounds of id.
	 *
	 * @param t - type of field
	 * @return true if type can be ignored, otherwise false
	 */
	private boolean ignoreType(FieldType<?> t) {
		int id = t.typeID();
		
		// basic types
		if (id <= 14) return true;
		
		// linear collections
		if (id <= 19) return ignoreType(((SingleArgumentType<?, ?>)t).groundType);
		
		// maps
		if (20 == id) return ignoreType(((MapType<?, ?>)t).keyType) && ignoreType(((MapType<?, ?>)t).valueType);
		
		// user types
		return false;
	}
}
