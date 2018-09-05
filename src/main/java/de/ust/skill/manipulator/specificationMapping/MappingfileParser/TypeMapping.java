package de.ust.skill.manipulator.specificationMapping.MappingfileParser;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class TypeMapping {
	private String typename;
	private String newTypename;
	private Map<String, String> fieldMapping = new HashMap<>();
	
	public TypeMapping(String typename) {
		this.typename = typename.toLowerCase();
	}

	public String getTypename() {
		return typename;
	}

	public void setNewTypename(String newTypename) {
		this.newTypename = newTypename.toLowerCase();
	}
	
	public String getNewTypename() {
		return newTypename;
	}
	
	public void addFieldMapping(String f1, String f2) {
		fieldMapping.put(f1.toLowerCase(), f2.toLowerCase());
	}

	public String getFieldMapping(String f) {
		if(fieldMapping.get(f) != null) return fieldMapping.get(f);
		return f;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(typename);
		if(newTypename != null) sb.append(" -> ").append(newTypename);
		sb.append(" {\n");
		for(Entry<String, String> e : fieldMapping.entrySet()) {
			sb.append("\t").append(e.getKey()).append(" -> ").append(e.getValue()).append("\n");
		}
		sb.append("}\n");
		return sb.toString();
	}
	
}
