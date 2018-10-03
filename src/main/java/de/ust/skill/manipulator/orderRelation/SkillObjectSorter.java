package de.ust.skill.manipulator.orderRelation;

import java.util.Arrays;
import java.util.Map;

import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.common.java.internal.StoragePool;
import de.ust.skill.common.java.internal.TypeHierarchyIterator;
import de.ust.skill.manipulator.internal.SkillFile;
import de.ust.skill.manipulator.internal.SkillState;

/**
 * This class provides function to normalize the graph with a given order relation.
 * 
 * @author olibroe
 *
 */
public class SkillObjectSorter {
	
	private SkillObjectSorter() {}
	
	/**
	 * Sort the SKilL-IDs of the objects according to the given Comparator.
	 * 
	 * @param sf - Skillfile with the objects to sort
	 * @param comp - comparator that defines the order
	 */
	@SuppressWarnings("unchecked")
	public static <T extends B, B extends SkillObject> void sort(SkillFile sf, SkillObjectComparator comp) {
		SkillState state = (SkillState) sf;
		
		// it is very important to load all lazy data fields
		// otherwise the data will be written in the same order as before, but the objects have different indices
		state.loadLazyData();
		
		for(StoragePool<?,?> pool : state.getTypes()) {
			if(pool.superPool == null) {
				TypeHierarchyIterator<T, B> it = new TypeHierarchyIterator<>((StoragePool<? extends T, B>)pool);
				int lowerBound = 0;
				SkillObject[] objects = pool.data;
				
				// we have to step through the type hierarchy because we need the lower bound in the sort function
				// the lower bound is equal to the base pool offset
				while(it.hasNext()) {
					StoragePool<?,?> subPool = it.next();
					
					Arrays.sort(objects, lowerBound, lowerBound + subPool.staticDataInstances, comp);
					
					lowerBound += subPool.staticDataInstances;
				}
				
			}
		}
	}
	
	/**
	 * Sort the SKilL-IDs of the objects according to the given Comparators.
	 * You can define a comparator for each type in the map (typename -> comparator).
	 * If there is no direct mapping for a type, a mapping for a superpool is searched.
	 * If there is no comparator at all, the object are not touched.
	 * 
	 * @param sf - Skillfile with the objects to sort
	 * @param compMap - typename->comparator mapping
	 */
	@SuppressWarnings("unchecked")
	public static <T extends B, B extends SkillObject> void sort(SkillFile sf, Map<String, SkillObjectComparator> compMap) {
		SkillState state = (SkillState) sf;
		
		// it is very important to load all lazy data fields
		// otherwise the data will be written in the same order as before, but the objects have different indices
		state.loadLazyData();

		for(StoragePool<?,?> pool : state.getTypes()) {
			if(pool.superPool == null) {
				TypeHierarchyIterator<T, B> it = new TypeHierarchyIterator<>((StoragePool<? extends T, B>)pool);
				int lowerBound = 0;
				SkillObject[] objects = pool.data;

				// we have to step through the type hierarchy because we need the lower bound in the sort function
				// the lower bound is equal to the base pool offset
				while(it.hasNext()) {
					StoragePool<?,?> subPool = it.next();
					SkillObjectComparator comp = null;
					int staticDataInstances = subPool.staticDataInstances;

					// search for a fitting comparator
					while(subPool != null) {
						comp = compMap.get(subPool.name());
						subPool = subPool.superPool;
					}
					
					if(comp != null) {
						Arrays.sort(objects, lowerBound, lowerBound + staticDataInstances, comp);
					}
					
					lowerBound += staticDataInstances;
				}
				
			}
		}
	}

}
