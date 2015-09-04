package com.bisol.involves;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Serialization strategy that outputs POJOs to a CSV stream.
 * The first line will contain the attribute's names, and subsequent lines will contain the corresponding values of a POJO.
 *
 * @author bisol
 */
public class CsvSerializer implements PojoSerializer{
	private final Logger logger = Logger.getLogger(getClass().getName());
	private Level logLevel = Level.INFO;

	/**
	 * Used to keep track of a attribute in the line. Useful when the supplied POJOs belong to more than one class.
	 */
	private Map<Integer, CsvAttributeKey> fieldPositions = new HashMap<>();

	private Object[] pojos;
	private String charset;
	private String lineTerminator = "\n";

	public CsvSerializer(Level logLevel) {
		this.logLevel = logLevel;
	}
	
	/**
	 * Allowed values: "\n" (default ) and "\r\n". 
	 */
	@Override
	public void setLineTerminator(String lineTerminator) {
		if(!lineTerminator.equals("\n") && !lineTerminator.equals("\r\n")){
			throw new IllegalArgumentException("Only \"\\n\" (default ) and \"\\r\\n\"");
		}
		logger.log(logLevel, "Line terminator set to " + lineTerminator);
		this.lineTerminator = lineTerminator;
	}

	@Override
	public void setCharset(String charset) {
		logger.log(logLevel, "Charset set to " + charset);
		this.charset = charset;
	}

	@Override
	public void setPojos(Object[] pojos) {
		logger.log(logLevel, "Received " + pojos.length);
		this.pojos = pojos;

		// Build field position map
		int nextPosition = 0;
		for(Object pojo : pojos){
			Class<? extends Object> pojoClass = pojo.getClass();
			String className = pojoClass.getSimpleName();
			
			logger.log(logLevel, "Mapping type " + pojoClass);
			nextPosition = getPojosAttributes(nextPosition, pojoClass, className);
		}
		logger.log(logLevel, "Mapping finished");
	}

	/*
	 * Builds a Map of POJOs attributes, according to appearance order
	 */
	private int getPojosAttributes(int nextPosition,  Class<?> currentClass, String pojoClassName) {
		START: for(Field attribute : currentClass.getDeclaredFields()){	
			String attributeName = attribute.getName();
			CsvAttributeKey newAttributeKey = new CsvAttributeKey(attributeName, pojoClassName, attribute.getType().getName());
			
			for(CsvAttributeKey existingAttributekey : fieldPositions.values()){
				if(existingAttributekey.equals(newAttributeKey)) {
					existingAttributekey.addOwnerClass(pojoClassName);
					continue START;
				}
					
			}
			
			logger.log(logLevel, "Found new attribute '" + attributeName + "' on POJO type '" + pojoClassName + "'");
			fieldPositions.put(nextPosition ++, newAttributeKey);
		}
		
		Class<?> superClass = currentClass.getSuperclass();
		if(superClass != null){
			logger.log(logLevel, "Mapping super classtype " + superClass.getName());
			return getPojosAttributes(nextPosition, superClass, pojoClassName);
		} else {
			return nextPosition;
		}
	}

	/**
	 * Uses reflection to access each POJO's attributes and outputs them to the supplied stream as CSV.
	 * If the supplied POJOs belong to different classes, columns will be reused when two POJOs of different classes
	 * have attributes with the same name 
	 */
	@Override
	public OutputStream serializePojosToStream(OutputStream outputStream) 
	throws UnsupportedEncodingException, IOException, ReflectiveOperationException {		
		logger.log(logLevel, "Writing CSV header");
		writeCsvHeader(outputStream);
		
		logger.log(logLevel, "Writing POJOs");
		for(Object pojo : pojos){
			serializeObject(outputStream, pojo);
		}
		
		return outputStream;
	}
	
	/*
	 * Writes a line with each attribute's name, comma separates.
	 */
	private void writeCsvHeader(OutputStream target) 
	throws UnsupportedEncodingException, IOException{
		for(int i = 0; i < fieldPositions.size(); i++){
			CsvAttributeKey attributeKey = fieldPositions.get(i);
			target.write(attributeKey.getAttributeName().getBytes(charset));
			
			// skip last attribute
			if(i < fieldPositions.size() - 1){
				target.write(",".getBytes(charset));
			}
		}
		
		target.write(lineTerminator.getBytes(charset));
	}
	
	/*
	 * Writes a POJO's attributes in the appropriate order, comma separated, and ends the line.
	 */
	private void serializeObject(OutputStream target, Object pojo) 
	throws ReflectiveOperationException, IllegalArgumentException, UnsupportedEncodingException, IOException{
		String className = pojo.getClass().getSimpleName();
		logger.log(logLevel, "Writing POJO of type " + className);
		for(int i = 0; i < fieldPositions.size(); i++){
			CsvAttributeKey attributeKey = fieldPositions.get(i);
			if(attributeKey.getOwnerClasses().contains(className)){
				Class<?> pojoClass = pojo.getClass();
				Field attribute = getAttributeField(pojoClass, attributeKey);
				
				attribute.setAccessible(true);
				Object attributeValue = attribute.get(pojo);
				if(attributeValue != null){
					String stringValue = String.valueOf(attributeValue);
					target.write(stringValue.getBytes(charset));
				}
			}
			
			// skip last attribute
			if(i < fieldPositions.size() - 1){
				target.write(",".getBytes(charset));
			}
		}
		target.write(lineTerminator.getBytes(charset));
	}

	/*
	 * Fetches the corresponding field from the POJO's class or its super classes
	 */
	private Field getAttributeField(Class<?> currentClass, CsvAttributeKey attributeKey) {
		Field attribute;
		try {
			attribute = currentClass.getDeclaredField(attributeKey.getAttributeName());
		} catch (NoSuchFieldException e) {
			logger.log(logLevel, "Searching superclass for attribute " + attributeKey.getAttributeName());
			return getAttributeField(currentClass.getSuperclass(), attributeKey);
		}

		return attribute;
	}
}