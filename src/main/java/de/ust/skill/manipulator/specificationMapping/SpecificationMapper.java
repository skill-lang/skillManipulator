package de.ust.skill.manipulator.specificationMapping;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.Semaphore;

import de.ust.skill.common.java.api.SkillException;
import de.ust.skill.common.java.internal.BasePool;
import de.ust.skill.common.java.internal.FieldDeclaration;
import de.ust.skill.common.java.internal.FieldIterator;
import de.ust.skill.common.java.internal.FieldType;
import de.ust.skill.common.java.internal.SkillObject;
import de.ust.skill.common.java.internal.StaticDataIterator;
import de.ust.skill.common.java.internal.StaticFieldIterator;
import de.ust.skill.common.java.internal.StoragePool;
import de.ust.skill.common.java.internal.fieldTypes.MapType;
import de.ust.skill.common.java.internal.fieldTypes.SingleArgumentType;
import de.ust.skill.common.java.internal.parts.Block;
import de.ust.skill.ir.TypeContext;
import de.ust.skill.manipulator.OutputPrinter;
import de.ust.skill.manipulator.internal.SkillFile;
import de.ust.skill.manipulator.internal.SkillState;
import de.ust.skill.manipulator.specificationMapping.mappingfileParser.MappingFileParser;
import de.ust.skill.manipulator.specificationMapping.mappingfileParser.ParseException;
import de.ust.skill.manipulator.specificationMapping.mappingfileParser.TypeMapping;
import de.ust.skill.manipulator.specificationMapping.messages.FieldIncompatibleInformation;
import de.ust.skill.manipulator.specificationMapping.messages.FieldNotFoundInformation;
import de.ust.skill.manipulator.specificationMapping.messages.MappingInformation;
import de.ust.skill.manipulator.specificationMapping.messages.TypeNotFoundInformation;
import de.ust.skill.manipulator.specificationMapping.messages.TypeProjectedInformation;
import de.ust.skill.manipulator.utils.FieldUtils;

/**
 * This class provides methods to map a Skill-Graph on a new specification.
 * 
 * @author olibroe
 *
 */
public class SpecificationMapper {
	
	// maps pools from old state to new state
	protected Map<StoragePool<?,?>, StoragePool<?,?>> poolMapping = new HashMap<>();
	
	// data structure that stores the mapping information for the types
	// in detail there are name to name mappings for types and fields
	private Map<String, TypeMapping> typeMappings = null;
	
	// store old and new Skillstate
	protected SkillState newState;
	private SkillState oldState;
	
	// lbpo maps of the old and new typesystem
	protected int[] newLbpoMap;
	private int[] oldLbpoMap;
	
	// the projection offset is needed if a type of the old state is projected
	// to another type in the new state, then the old type is mapped to a value that represents
	// the relative position of the projected type in the type it is projected to 
	private int[] projectionOffsetMap;
	
	// stores mapping information
	private Set<MappingInformation> mappingLog = new HashSet<>();
	private boolean returnState = true;
	
	/**
	 * Add Mapping Information to the log of this specification mapping process.
	 * 
	 * @param info - the information to be stored
	 */
	public void addToMappingLog(MappingInformation info) {
		mappingLog.add(info);
	}
	
	/**
	 * Get the mapping informations of this mapping process.
	 * 
	 * @return - set of mapping informations
	 */
	public Set<MappingInformation> getMappingLog() {
		return mappingLog;
	}
	
	private String logToString() {
		StringBuilder sb = new StringBuilder();
		boolean heading = true;
		for(MappingInformation info : mappingLog) {
			if(info instanceof FieldIncompatibleInformation) {
				if(heading) {
					sb.append("Field incompatibility errors:\n");
					heading = false;
				}
				sb.append("\t").append(info).append("\n");
			}
		}
		heading = true;
		for(MappingInformation info : mappingLog) {
			if(!(info instanceof FieldIncompatibleInformation)) {
				if(heading) {
					sb.append("Information about the mapping:\n");
					heading = false;
				}
				sb.append("\t").append(info).append("\n");
			}
		}
		
		return sb.toString();
	}
	
	/**
	 * Maps the given Skillfile sf on the new Specification tc.
	 * The method creates a new Skillfile that is created at targetPath.
	 * The parameter mappigfile is a string with a path to a mapping file that contains name mappings for types
	 * and fields. 
	 * 
	 * @param tc - TypeContext of the new specification
	 * @param sf - Skillfile to map
	 * @param targetPath - Path where the new Skillfile should be created
	 * @param mappingfile - Mappingfile with name to name mappings
	 * @return - new SKillfile with typesystem as in the TypeContext and information from Skillfile sf
	 * @throws ParseException - thrown if the mapping file given by the string mappingfile is syntactically wrong
	 * @throws FileNotFoundException - thrown if string mappingfile is not the path to a mapping file
	 * @throws IOException - thrown if the new Skillfile could not be created
	 * @throws InterruptedException - thrown if the object allocation in the new Skillfile fails
	 * @throws SkillException - thrown if some restriction check fails
	 */
	public SkillFile map(TypeContext tc, SkillFile sf, Path targetPath, String mappingfile)
			throws ParseException, FileNotFoundException, IOException, InterruptedException, SkillException {
		
		typeMappings = MappingFileParser.parseFile(mappingfile);
		
		return map(tc, sf, targetPath);
	}

	/**
	 * Maps the given Skillfile sf on the new Specification tc.
	 * The method creates a new Skillfile that is created at targetPath.
	 * 
	 * @param tc - TypeContext of the new specification
	 * @param sf - Skillfile to map
	 * @param targetPath - Path where the new Skillfile should be created
	 * @param mappingfile - Mappingfile with name to name mappings
	 * @return - new SKillfile with typesystem as in the TypeContext and information from Skillfile sf
	 * @throws IOException - thrown if the new Skillfile could not be created
	 * @throws InterruptedException - thrown if the object allocation in the new Skillfile fails
	 * @throws SkillException - thrown if some restriction check fails
	 */
	public SkillFile map(TypeContext tc, SkillFile sf, Path targetPath) 
			throws IOException,InterruptedException, SkillException {
		oldState = (SkillState)sf;
		
		// create the new typesystem according to the type context
		newState = StateCreator.createNewState(tc, targetPath);
		
		// map the old typesystem to the new typesystem
		mapStates();
		
		// fix types to have faster size operations
		StoragePool.fixed(newState.getTypes());
		
		// transfer the objects
		transferObjects();
	
		// transfer the field data
		transferFields();
		
		// unfix types
		StoragePool.unfix(newState.getTypes());
		
		// check restrictions
		newState.check();
		
		OutputPrinter.println(logToString());
		
		// only return state if there are no critical failures
		if(returnState) return newState;
		else return null;
	}
		
	/**
	 * Maps the old typesystem to the new typesystem.
	 * This is a type to type mapping.
	 * Types are mapped by name equivalence (mapping file can change names).
	 * If no equivalent name can be found, the method tries to map the type to one of his supertypes.
	 * If no mapping can be found at all, the type is mapped to null.
	 */
	private void mapStates() {
		StoragePool<?,?> newPool;
		
		// while we are mapping we calculate the bpos and projectionOffsets for the old types
		oldLbpoMap = new int[oldState.getTypes().size()];
		projectionOffsetMap = new int[oldState.getTypes().size()];
		
		int lbpo = 0;
		
		for(StoragePool<?, ?> oldPool : oldState.getTypes()) {
			// get name equivalent pool in new state
			newPool = newState.pool(getTypeName(oldPool.name()));
			
			// if new pool is null, there could not be found a direct mapping
			if(newPool == null) {
				// map pool to mapping of superpool
				// if superpool has no mapping, the result is null -> no mapping
				newPool = poolMapping.get(oldPool.superPool);
				if(newPool != null) {
					// if we can project the old pool, we need the offset map later to refer to the types instances
					projectionOffsetMap[oldPool.typeID-32] = newPool.staticDataInstances;
					newPool.staticDataInstances += oldPool.staticDataInstances;
					
					addToMappingLog(new TypeProjectedInformation(oldPool, newPool));
				} else {
					addToMappingLog(new TypeNotFoundInformation(oldPool));
				}
			} else {		
				newPool.staticDataInstances += oldPool.staticDataInstances;
			}
			
			// store mapping
			poolMapping.put(oldPool, newPool);
			
			// create oldLbpoMap
			if(oldPool.superPool == null) {
				lbpo = 0;
			}
			oldLbpoMap[oldPool.typeID-32] = lbpo;
			lbpo += oldPool.staticDataInstances;
		}
	}

	/**
	 * Transfer the objects from old state to new state. 
	 * This is done by transferring the object information and then allocating the objects in new typesystem.
	 * 
	 * @throws InterruptedException - thrown if there happens an error while allocating objects
	 */
	@SuppressWarnings("unchecked")
	private <T> void transferObjects() throws InterruptedException {	
		// calculate new lbpos
		newLbpoMap = new int[newState.getTypes().size()];
		int lbpo = 0;
		
		// loop over new types and create bpos and add object information 
		for(StoragePool<?,?> newPool : newState.getTypes()) {
			if(newPool.superPool == null) {
				lbpo = 0;
			}
			newLbpoMap[newPool.typeID-32] = lbpo;
			// this adds the object information
			newPool.blocks().add(new Block(lbpo, newPool.size(), newPool.staticDataInstances));
			lbpo += newPool.staticDataInstances;
		}
	
		// allocate new skillobjects
		final Semaphore barrier = new Semaphore(0, false);
		int reads = 0;
		for(StoragePool<?,?> newPool : newState.getTypes()) {
			if(newPool instanceof BasePool<?>) {
				reads += ((BasePool<?>) newPool).performAllocations(barrier);
			}
		}
		barrier.acquire(reads);
		
		// set default values
		// the default values are overwritten by field data if there is field data
		for(StoragePool<?,?> newPool : newState.getTypes()) {
			StaticFieldIterator fit = newPool.fields();
			while(fit.hasNext()) {
				FieldDeclaration<T, ?> next = (FieldDeclaration<T, ?>) fit.next();
				Object defValue = FieldUtils.getDefaultValue(next);
				if(next.type().typeID < 32) {
					ValueConversion conv = dispatchConversion(next.type());
					defValue = conv.convert(defValue);
				}
				for(SkillObject o : next.owner()) {
					next.set(o, (T)defValue);
				}
			}
		}
	}
	
	/**
	 * Transfer the field values.
	 */
	private void transferFields() {
		FieldCompatibilityChecker checker = new FieldCompatibilityChecker(this);
		FieldIterator oldFieldIt;
		FieldDeclaration<?,?> oldField;
		FieldDeclaration<?,?> newField;
	
		for(StoragePool<?,?> oldPool : oldState.getTypes()) {
			// get mapped pool, if not null we want to transfer the fields
			StoragePool<?,?> newPool = poolMapping.get(oldPool);
			if(newPool != null) {
				
				// iterate over all fields of the old type
				oldFieldIt = oldPool.allFields();
				while(oldFieldIt.hasNext()) {
					oldField = oldFieldIt.next();
					
					// skip constant fields
					if(oldField.type().typeID > 4) {
						String searchString = getFieldName(oldField);
						
						// search for right field in fields of new type
						newField = searchField(searchString, newPool);
						
						if(newField != null) {
							// if we have found a field, check if the fields are compatible and transfer data
							if(checker.fieldsCompatible(oldField, newField, oldPool)) 
								transferFieldData(oldField, newField, oldPool);
							else {
								// if the fields are not compatible, we do not want to create a new state
								// because this is a critical failure
								addToMappingLog(new FieldIncompatibleInformation(oldField, newField, oldPool, newPool));
								returnState = false;
							}
						} else {
							addToMappingLog(new FieldNotFoundInformation(oldField, oldPool, newPool));
						}
					}
				}
			}
		}
	}

	/**
	 * Search field with name fieldname in the fields of type new pool.
	 * The fields inherited by supertypes are included in the search.
	 * 
	 * @param fieldname - name of the field to search
	 * @param newPool - type to search in
	 * @return field if found, otherwise null
	 */
	private FieldDeclaration<?,?> searchField(String fieldname, StoragePool<?, ?> newPool) {
		FieldIterator newFieldIt = newPool.allFields();
		FieldDeclaration<?, ?> newField;

		while(newFieldIt.hasNext()) {
			newField = newFieldIt.next();
			if(newField.name().equals(fieldname)) return newField;		
		}
		
		return null;
	}

	/**
	 * Transfer the field data. The compatibility of fields is checked before this step.
	 * The data is transferred for every type that has the field independently. This is because a field in the old
	 * typesystem could be splitted in multiple field in the new typsystem.
	 * 
	 * @param oldField - data from this field
	 * @param newField - data to this field
	 * @param oldPool - field data for the objects of this type
	 */
	@SuppressWarnings("unchecked")
	private <T> void transferFieldData(FieldDeclaration<?, ?> oldField, FieldDeclaration<T, ?> newField,
			StoragePool<?,?> oldPool) {	
		
		// value conversion is necessary because the field types could be different
		ValueConversion valueConv = dispatchConversion(newField.type());
		
		StaticDataIterator<?> sit = oldPool.staticInstances();
		while(sit.hasNext()) {
			SkillObject oldObj = sit.next();
			SkillObject newObj = calculateNewSkillObject(oldObj);
			
			Object value = valueConv.convert(oldField.get(oldObj));
	
			newField.set(newObj,(T)value);
		}
	}
	
	/**
	 * Return the typename of the type in the new typesystem. 
	 * This could be influenced by mapping file.
	 * 
	 * @param name - name of type in old typesystem
	 * @return name in new typesystem
	 */
	private String getTypeName(String name) {
		if(typeMappings != null) {
			TypeMapping tm = typeMappings.get(name);
			if(tm != null) {
				String newName = tm.getNewTypename();
				if(newName != null) return newName;
			}
		}
		return name;
	}
	
	/**
	 * Return the fieldname of the field in the new typesystem. 
	 * This could be influenced by mapping file.
	 * 
	 * @param oldField - field in old typesystem
	 * @return fieldname in new typesystem
	 */
	private String getFieldName(FieldDeclaration<?, ?> oldField) {
		if(typeMappings != null) {
			TypeMapping tm = typeMappings.get(oldField.owner().name());
			if(tm != null) {
				// if there is no field mapping the getFieldMapping method returns the string of the parameter
				String name = tm.getFieldMapping(oldField.name());
				return name;
			}
		}
		return oldField.name();
	}
	
	/**
	 * Calculate the corresponding Skillobject in the new typesystem.
	 * Call this method only for Skillobjects that are present in the new typesystem, otherwise a nullpointer
	 * exception is thrown.
	 * 
	 * @param oldObject - the object for which we want to calculate the corresponding SKillobject
	 * @return calculated Skillobject
	 */
	protected SkillObject calculateNewSkillObject(SkillObject oldObject) {
		StoragePool<?,?> oldPool = oldState.pool(oldObject.skillName());
		StoragePool<?,?> newPool = poolMapping.get(oldPool);
		
		// skill id - lbpo(oldPool) => relative id in oldPool
		// relative id in oldPool + lbpo(newPool) => relative id in new pool
		// add projection offset if newPool is now part of another pool (projection)
		int id = oldObject.getSkillID() - oldLbpoMap[oldPool.typeID-32]
				+ newLbpoMap[newPool.typeID-32] + projectionOffsetMap[oldPool.typeID-32];

		return newPool.getByID(id);
	}
	
	private final ValueConversion usertypeConv = new UsertypeConversion();

	/**
	 * Returns the data conversion interface for every possible field type.
	 * 
	 * @param newType - type of the field
	 * @return value conversion interface
	 */
	private ValueConversion dispatchConversion(FieldType<?> newType) {
		int id = newType.typeID;
		if(id == 6 || id == 14) return StandardConversion.get();
		else if(7 <= id && id <= 13) return NumberConversion.get(id);
		else if(15 <= id && id <= 19) return new LinearCollectionConversion(newType);
		else if(id == 20) return new MapConversion(newType);
		else return usertypeConv;
	}
	
	/**
	 * Value Conversion only needs convert method
	 * @author olibroe
	 *
	 */
	private static abstract class ValueConversion {
		abstract Object convert(Object o);
	}
	
	/**
	 * Conversion for bool and string simply returns objects.
	 * @author olibroe
	 *
	 */
	private static class StandardConversion extends ValueConversion {
		private final static StandardConversion sConv = new StandardConversion();
		
		public static ValueConversion get() {
			return sConv;
		}
		
		@Override
		Object convert(Object o) {
			return o;
		}
	}
	
	/**
	 * Number conversion is done through methods of java.lang.Number
	 * @author olibroe
	 *
	 */
	private static abstract class NumberConversion extends ValueConversion{
		private final static ByteConversion byteConv = new ByteConversion();
		private final static ShortConversion shortConv = new ShortConversion();
		private final static IntConversion intConv = new IntConversion();
		private final static LongConversion longConv = new LongConversion();
		private final static FloatConversion floatConv = new FloatConversion();
		private final static DoubleConversion doubleConv = new DoubleConversion();
		
		static NumberConversion get(int id) {
			switch(id) {
			case 7: return byteConv;
			case 8: return shortConv;
			case 9: return intConv;
			case 10: 
			case 11:
				return longConv;
			case 12: return floatConv;
			default:
				// note: this is case 13
				return doubleConv;
			}
		}
		
		private static class ByteConversion extends NumberConversion {
			@Override
			Object convert(Object o) {
				return ((Number)o).byteValue();
			}
		}
		
		private static class ShortConversion extends NumberConversion {
			@Override
			Object convert(Object o) {
				return ((Number)o).shortValue();
			}
		}
		
		private static class IntConversion extends NumberConversion {
			@Override
			Object convert(Object o) {
				return ((Number)o).intValue();
			}
		}
		
		private static class LongConversion extends NumberConversion {
			@Override
			Object convert(Object o) {
				return ((Number)o).longValue();
			}
		}
		
		private static class FloatConversion extends NumberConversion {
			@Override
			Object convert(Object o) {
				return ((Number)o).floatValue();
			}
		}
		
		private static class DoubleConversion extends NumberConversion {
			@Override
			Object convert(Object o) {
				return ((Number)o).doubleValue();
			}
		}
	}
	
	/**
	 * Linear collection conversion creates a new collection for every old collection.
	 * @author olibroe
	 *
	 */
	private class LinearCollectionConversion extends ValueConversion {
		private final ValueConversion baseTypeConversion;
		private final CollectionCreator collectionCreator;
		
		private final ArrayCreator arrayCreator = new ArrayCreator();
		private final ListCreator listCreator = new ListCreator();
		private final SetCreator setCreator = new SetCreator();
		
		private LinearCollectionConversion(FieldType<?> newType) {
			int id = newType.typeID;
			if(id == 15 || id == 17) this.collectionCreator = arrayCreator;
			else if(id == 18) this.collectionCreator = listCreator;
			else this.collectionCreator = setCreator;
			
			this.baseTypeConversion = dispatchConversion(((SingleArgumentType<?, ?>)newType).groundType);
		}
		
		@Override
		Object convert(Object o) {
			if(o == null) return null;
			
			Collection<Object> newValues = collectionCreator.create((Collection<?>) o);
			
			for(Object obj : (Collection<?>)o) {
				newValues.add(baseTypeConversion.convert(obj));
			}
			return newValues;
		}
		
		private abstract class CollectionCreator {
			abstract Collection<Object> create(Collection<?> oldCollection);
		}
		
		private class ArrayCreator extends CollectionCreator {
			@Override
			public Collection<Object> create(Collection<?> oldCollection) {
				return new ArrayList<>(oldCollection.size());
			}
		}
		
		private class ListCreator extends CollectionCreator {
			@Override
			public Collection<Object> create(Collection<?> oldCollection) {
				return new LinkedList<>();
			}
		}
		
		private class SetCreator extends CollectionCreator {
			@Override
			public Collection<Object> create(Collection<?> oldCollection) {
				return new HashSet<>(oldCollection.size());
			}
		}
		
	}
	
	/**
	 * Creates a new Map for the old map.
	 * @author olibroe
	 *
	 */
	private class MapConversion extends ValueConversion {
		private final ValueConversion keyTypeConversion;
		private final ValueConversion valueTypeConversion;
		
		private MapConversion(FieldType<?> newType) {
			this.keyTypeConversion = dispatchConversion(((MapType<?, ?>)newType).keyType);
			this.valueTypeConversion = dispatchConversion(((MapType<?, ?>)newType).valueType);
		}
		
		@Override
		Object convert(Object o) {
			if(o == null) return null;
			
			Map<Object,Object> newMap = new HashMap<>(((Map<?, ?>) o).size());

			for(Entry<?, ?> oldEntry : ((Map<?, ?>) o).entrySet()) {
				newMap.put(keyTypeConversion.convert(oldEntry.getKey()), valueTypeConversion.convert(oldEntry.getValue()));
			}
			
			return newMap;
		}
	}
	
	/**
	 * Usertype conversion only has to call the calculateNewSkillObject method. 
	 * @author olibroe
	 *
	 */
	private class UsertypeConversion extends ValueConversion {	
		@Override
		Object convert(Object o) {
			if(o == null) return null;
			return calculateNewSkillObject((SkillObject) o);
		}
	}

}
