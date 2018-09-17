package de.ust.skill.manipulator.orderRelation;

import java.util.Arrays;
import java.util.Map;

import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.common.java.internal.StoragePool;
import de.ust.skill.common.java.internal.TypeHierarchyIterator;
import de.ust.skill.manipulator.internal.SkillFile;
import de.ust.skill.manipulator.internal.SkillState;

public class SkillObjectSorter {
	
	private SkillObjectSorter() {}
	
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
				
				while(it.hasNext()) {
					StoragePool<?,?> subPool = it.next();
					
					Arrays.sort(objects, lowerBound, lowerBound + subPool.staticDataInstances, comp);
					
					lowerBound += subPool.staticDataInstances;
				}
				
			}
		}
	}
	
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

				while(it.hasNext()) {
					StoragePool<?,?> subPool = it.next();
					SkillObjectComparator comp = null;
					int staticDataInstances = subPool.staticDataInstances;

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
