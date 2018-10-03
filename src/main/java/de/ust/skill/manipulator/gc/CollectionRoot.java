package de.ust.skill.manipulator.gc;

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
public class CollectionRoot {
	// this constant is the ID that represents all IDs of a type
	public static final int ALL_IDS = 0;

	// type of root
	private String type;
	// id of root; can be ALL_IDs to specify all objects o a type
	private int id;

	/**
	 * Create a collection root for the garbage collection.
	 * This root represents all objects of the given type.
	 * 
	 * @param type - typename of type
	 */
	public CollectionRoot(String type) {
		this.type = type;
		this.id = ALL_IDS;
	}

	/**
	 * Create a collection root for the garbage collection.
	 * This root represents one object of the given type.
	 * 
	 * If the id is smaller than 1, all objects of the given type are roots.
	 * This is because SKilL-IDs are positive natural numbers.
	 * 
	 * @param type - typename of type
	 * @param id - id of object that should be the root
	 */
	public CollectionRoot(String type, int id) {
		this.type = type;
		if(id < 1)
			this.id = ALL_IDS;
		else
			this.id = id;
	}

	public String getType() {
		return type;
	}

	public int getId() {
		return id;
	}
}

