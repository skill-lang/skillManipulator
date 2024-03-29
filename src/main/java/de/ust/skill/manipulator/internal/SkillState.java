package de.ust.skill.manipulator.internal;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;

import de.ust.skill.common.java.api.SkillException;
import de.ust.skill.common.java.internal.FieldDeclaration;
import de.ust.skill.common.java.internal.FieldIterator;
import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.common.java.internal.StoragePool;
import de.ust.skill.common.java.internal.StringPool;
import de.ust.skill.common.java.internal.fieldTypes.Annotation;
import de.ust.skill.common.java.restrictions.FieldRestriction;
import de.ust.skill.common.java.restrictions.TypeRestriction;
import de.ust.skill.common.jvm.streams.FileInputStream;
import de.ust.skill.manipulator.OutputPrinter;

/**
 * Internal implementation of SkillFile.
 *
 * @author Timm Felden
 * @note type access fields start with a capital letter to avoid collisions
 * 
 * @modified by Oliver Brösamle
 */
public final class SkillState extends de.ust.skill.common.java.internal.SkillState implements SkillFile {

    /**
     * Create a new skill file based on argument path and mode.
     *
     * @throws IOException
     *             on IO and mode related errors
     * @throws SkillException
     *             on file or specification consistency errors
     * @note suppress unused warnings, because sometimes type declarations are
     *       created, although nobody is using them
     */
    public static SkillState open(Path path, Mode... mode) throws IOException, SkillException {
        ActualMode actualMode = new ActualMode(mode);
        try {
            switch (actualMode.open) {
            case Create:
                // initialization order of type information has to match file
                // parser
                // and can not be done in place
                StringPool strings = new StringPool(null);
                ArrayList<StoragePool<?, ?>> types = new ArrayList<>(0);
                Annotation annotation = new Annotation(types);

                return new SkillState(new HashMap<>(), strings, annotation,
                        types, FileInputStream.open(path, false), actualMode.close);

            case Read:
                Parser p = new Parser(FileInputStream.open(path, actualMode.close == Mode.ReadOnly));
                return p.read(SkillState.class, actualMode.close);

            default:
                throw new IllegalStateException("should never happen");
            }
        } catch (SkillException e) {
            // rethrow all skill exceptions
            throw e;
        } catch (Exception e) {
            throw new SkillException(e);
        }
    }

    public SkillState(HashMap<String, 
    		StoragePool<?, ?>> poolByName, 
    		StringPool strings, 
            Annotation annotationType, 
            ArrayList<StoragePool<?, ?>> types, 
            FileInputStream in, 
            Mode mode) {
        super(strings, in.path(), mode, types, poolByName, annotationType);

        for (StoragePool<?, ?> t : types)
            poolByName.put(t.name(), t);

        finalizePools(in);
    }

    /**
     * 
     * @return StoragePools in type order
     */
	public ArrayList<StoragePool<?, ?>> getTypes() {
		return this.types;
	}
	
	/**
	 * Print the state on the command line.
	 * 
	 */
	public void prettyPrint() {
		StringBuilder sb = new StringBuilder();
		for(StoragePool<?, ?> pool : types) {
			for(TypeRestriction r : pool.restrictions) sb.append(r.toString()).append("\n");
			sb.append(pool.toString()).append("(").append(pool.size()).append(")");
			if(pool.superPool != null) sb.append(":").append(pool.superPool);
			sb.append(" {\n");
			
			FieldIterator fit = pool.allFields();
			while(fit.hasNext()) {
				FieldDeclaration<?, ?> f = fit.next();
				for(FieldRestriction<?> r : f.restrictions) sb.append("\t").append(r.toString()).append("\n");
				sb.append("\t").append(f.type().toString()).append(" ").append(f.name());
				for(SkillObject o : pool) {
					sb.append(" ").append(f.get(o));
				}
				sb.append("\n");
			}		
			
			sb.append("}\n\n");
		}
		OutputPrinter.println(sb.toString());
	}
}