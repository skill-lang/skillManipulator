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
	static final int ALL_IDS = 0;

	String type;
	int id;

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

