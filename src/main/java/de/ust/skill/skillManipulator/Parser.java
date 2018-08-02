package de.ust.skill.skillManipulator;

import java.util.ArrayList;
import java.util.HashSet;
import de.ust.skill.common.jvm.streams.FileInputStream;

import de.ust.skill.common.java.internal.BasePool;
import de.ust.skill.common.java.internal.FileParser;
import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.common.java.internal.StoragePool;
import de.ust.skill.common.java.restrictions.DefaultValue;
import de.ust.skill.common.java.restrictions.TypeRestriction;

public final class Parser extends FileParser{

	Parser(FileInputStream in) {
		super(in, 1000);
	}
	
	/**
     * allocate correct pool type and add it to types
     */
    @SuppressWarnings("unchecked")
	static <T extends B, B extends SkillObject, P extends StoragePool<T, B>> P newPool(String name,
            StoragePool<?, ?> superPool, ArrayList<StoragePool<?, ?>> types) {
        try {
            switch (name) {
            default:
                if (null == superPool)
                    return (P) (superPool = new BasePool<T>(types.size(), name, StoragePool.noKnownFields, noAutoFields()));
                else
                    return (P) (superPool = superPool.makeSubPool(types.size(), name));
            }
        } finally {
            types.add(superPool);
        }
    }
	
	@Override
	protected <T extends B, B extends SkillObject> StoragePool<T, B> newPool(
			String name,
			StoragePool<? super T, B> superPool,
			HashSet<TypeRestriction> restrictions) {
		StoragePool<T,B> pool = newPool(name, superPool, types);
		for(TypeRestriction r : restrictions) {
			pool.addRestriction(r);
			if(r instanceof DefaultValue) pool.defaultValue = (DefaultValue<?>)r;
		}
		return pool;
	}

}
