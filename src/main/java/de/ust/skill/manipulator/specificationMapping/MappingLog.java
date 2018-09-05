package de.ust.skill.manipulator.specificationMapping;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import de.ust.skill.common.java.internal.FieldDeclaration;
import de.ust.skill.common.java.internal.StoragePool;

public class MappingLog {
	private final static MappingLog log = new MappingLog();
	
	private MappingLog() {};
	
	private Map<StoragePool<?,?>, Set<MappingMessage>> errorPerType = new HashMap<>();
	
	private Set<MappingMessage> getErrorSet(StoragePool<?,?> pool) {
		Set<MappingMessage> retval = errorPerType.get(pool);
		if(retval == null) {
			retval = new HashSet<>();
			errorPerType.put(pool, retval);
		}
		return retval;
	}
	
	public static void clearLog() {
		log.errorPerType.clear();
	}
	
	public static String printLog() {
		StringBuilder sb = new StringBuilder();
		for(Entry<StoragePool<?,?>, Set<MappingMessage>> entry : log.errorPerType.entrySet()) {
			StoragePool<?, ?> pool = entry.getKey();
			sb.append("Messages for old type ").append(pool.name()).append(" (").append(pool.staticSize()).append(" static instances)").append(":\n");
			for(MappingMessage error : entry.getValue()) {
				sb.append("\t").append(error.print()).append("\n");
			}
		}
		
		return sb.toString();
	}
	
	public static void genTypeNotFoundError(StoragePool<?,?> type) {
		Set<MappingMessage> errors = log.getErrorSet(type);
		errors.add(TypeNotFoundError.get());
	}
	
	public static void genFieldNotFoundError(FieldDeclaration<?,?> f) {
		Set<MappingMessage> errors = log.getErrorSet(f.owner());
		errors.add(new FieldNotFoundError(f));
	}
	
	public static void genFieldIncompatibleError(FieldDeclaration<?,?> oldF, FieldDeclaration<?,?> newF) {
		Set<MappingMessage> errors = log.getErrorSet(oldF.owner());
		errors.add(new FieldIncompatibleError(oldF, newF));
	}
	
	public static void genIntFloatWarning(FieldDeclaration<?,?> oldF, FieldDeclaration<?,?> newF) {
		Set<MappingMessage> errors = log.getErrorSet(oldF.owner());
		errors.add(new IntFloatWarning(oldF, newF));
	}
	
	public static void genProjectionMessage(StoragePool<?,?> type, StoragePool<?,?> projectionType) {
		Set<MappingMessage> errors = log.getErrorSet(type);
		errors.add(new TypeProjectionMessage(projectionType));
	}
	
	private interface MappingMessage {
		String print();
	}
	
	private static class TypeNotFoundError implements MappingMessage {
		private final static TypeNotFoundError instance = new TypeNotFoundError();
		
		private TypeNotFoundError() {}
		
		public static TypeNotFoundError get() {
			return instance;
		}

		@Override
		public String print() {
			return new String("Type not found");
		}
		
	}
	
	private static class FieldNotFoundError implements MappingMessage {
		private final FieldDeclaration<?,?> f;
		
		FieldNotFoundError(FieldDeclaration<?,?> f) {
			this.f = f;
		}

		@Override
		public String print() {
			return new String("Field " + f + " not found");
		}
		
		@Override
		public boolean equals(Object o) {
			if(o instanceof FieldNotFoundError) {
				return this.f.equals(((FieldNotFoundError) o).f);
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return f.hashCode();
		}
		
	}
	
	private static class FieldIncompatibleError implements MappingMessage {
		private final FieldDeclaration<?,?> oldF;
		private final FieldDeclaration<?,?> newF;
		
		FieldIncompatibleError(FieldDeclaration<?,?> oldF, FieldDeclaration<?,?> newF) {
			this.oldF = oldF;
			this.newF = newF;
		}

		@Override
		public String print() {
			return new String("Field " + oldF + " not compatible with field " + newF);
		}
		
		@Override
		public boolean equals(Object o) {
			if(o instanceof FieldIncompatibleError) {
				return this.oldF.equals(((FieldIncompatibleError) o).oldF);
			}
			return false;
		}
		
		@Override
		public int hashCode() {
			return oldF.hashCode();
		}
	}
	
	private static class IntFloatWarning implements MappingMessage {
		private final FieldDeclaration<?,?> oldF;
		private final FieldDeclaration<?,?> newF;
		
		IntFloatWarning(FieldDeclaration<?,?> oldF, FieldDeclaration<?,?> newF) {
			this.oldF = oldF;
			this.newF = newF;
		}

		@Override
		public String print() {
			return new String("Warning: Converting from " + oldF + " to " + newF + " leads to loss of precision");
		}
		
	}
	
	private static class TypeProjectionMessage implements MappingMessage {
		private final StoragePool<?,?> projectionType;
		
		TypeProjectionMessage(StoragePool<?,?> projectionType) {
			this.projectionType = projectionType;
		}

		@Override
		public String print() {
			return new String("Projection to type " + projectionType);
		}
		
	}
	

}
