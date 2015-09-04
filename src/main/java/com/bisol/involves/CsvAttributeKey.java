package com.bisol.involves;

import java.util.HashSet;
import java.util.Set;

/**
 * Holds an attribute's name and type, and a set of classes that share this combination.
 * Used to support serializing a collection of POJOs of different classes.
 * 
 * @author bisol
 */
class CsvAttributeKey {
	private String attributeName, type;
	private Set<String> ownerClasses = new HashSet<>();
	
	public CsvAttributeKey(String attributeName, String ownerClass, String type){
		setAttributeName(attributeName);
		addOwnerClass(ownerClass);
		setType(type);
	}

	public String getAttributeName() {
		return attributeName;
	}

	public void setAttributeName(String attributeName) {
		this.attributeName = attributeName;
	}
	
	public String getType() {
		return type;
	}
	
	public void setType(String type) {
		this.type = type;
	}

	public Set<String> getOwnerClasses() {
		return ownerClasses;
	}

	public void addOwnerClass(String ownerClass) {
		this.ownerClasses.add(ownerClass);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((attributeName == null) ? 0 : attributeName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CsvAttributeKey other = (CsvAttributeKey) obj;
		if (attributeName == null) {
			if (other.attributeName != null)
				return false;
		} else if (!attributeName.equals(other.attributeName))
			return false;
		return true;
	}
}