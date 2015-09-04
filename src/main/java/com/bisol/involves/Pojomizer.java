package com.bisol.involves;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.LoggingMXBean;

/**
 * A library for serialization of POJOs.
 * Currently only implements serialization to CSV files or streams.
 *
 * @author bisol
 */
public class Pojomizer {
	// Serialization strategies supported
	public static final int SERIALIZATION_TARGET_CSV = 0;
	
	private final Logger logger = Logger.getLogger(getClass().getName());
	private Level logLevel = Level.INFO;
	
	private String charset = "utf8";
	private int strategy = SERIALIZATION_TARGET_CSV;

	/**
	 * Target Character Set for serialization.
	 * @param charset
	 */
	public void setCharset(String charset) {
		this.charset = charset;
	}
	
	/**
	 * Sets the serialization strategy to use.
	 * @param strategy
	 */
	public void setStrategy(int strategy) {
		this.strategy = strategy;
	}
	
	/**
	 * Allows to control the log severity  
	 * @param logLevel
	 */
	public void setLogLevel(Level logLevel) {
		this.logLevel = logLevel;
	}
	
	/**
	 * Serializes a collection of POJOs to a file using the specified strategy.
	 * @param target file to write to. If it exists, will try to clear it.
	 * @param objects An array of objects which each attribute is a simple type (Strings, enums, primitives and primitive wrappers)
	 * @return a FileOutputStream to the specified File. It is the caller's responsibility to close it.
	 * @throws IOException
	 * @throws ReflectiveOperationException
	 */
	public OutputStream serializePojosToFile(File target, Collection<?> objects) 
	throws IOException, ReflectiveOperationException {
		return this.serializePojosToFile(target, objects.toArray(new Object[objects.size()]));
	}
	
	/**
	 * Serializes one or more POJOs to a file using the specified strategy.
	 * @param target file to write to. If it exists, will try to clear it.
	 * @param objects An array of objects which each attribute is a simple type (Strings, enums, primitives and primitive wrappers)
	 * @return a FileOutputStream to the specified File. It is the caller's responsibility to close it.
	 * @throws IOException
	 * @throws ReflectiveOperationException
	 */
	public OutputStream serializePojosToFile(File target, Object... objects) 
	throws IOException, ReflectiveOperationException{
		if(target.exists()){
			target.delete();
		}
		
		logger.log(logLevel, "Openining file output stream");
		FileOutputStream fos = new FileOutputStream(target);
		
		try{
			return serializePojosToStream(fos, objects);
		} catch(IOException | ReflectiveOperationException | RuntimeException e){
			logger.log(logLevel, "Closing file output stream");
			// close the stream, since the caller won't be able to
			fos.close();
			throw e;
		}
	}
	
	/**
	 * Serializes a collection of POJOs to a OutputStream using the specified serializer.
	 * @param target OutputStream to write to.
	 * @param objects An array of objects which each attribute is a simple type (Strings, enums, primitives and primitive wrappers)
	 * @return the supplied OutputStream. It is the caller's responsibility to close it.
	 * @throws IOException
	 * @throws ReflectiveOperationException
	 */
	public OutputStream serializePojosToStream(OutputStream target, Object... objects) 
	throws IOException, ReflectiveOperationException{
		logger.log(logLevel, "Validating received POJOs");
		validatePojos(objects);

		logger.log(logLevel, "Building serializer");
		PojoSerializer serializer = newSerializer(strategy);
		
		if(charset != null){
			serializer.setCharset(charset);
		}
		
		serializer.setPojos(objects);
		if(charset != null){
			serializer.setCharset(charset);
		}
		
		logger.log(logLevel, "Serializing POJOS");
		return serializer.serializePojosToStream(target);
	}

	/*
	 * PojoSerializer factory method. Could be extracted to a new class, but that's over-engineering for now 
	 */
	private PojoSerializer newSerializer(int strategy) {
		PojoSerializer serializer;
		switch (strategy) {
		case SERIALIZATION_TARGET_CSV:
			serializer = new CsvSerializer(logLevel);
			logger.log(logLevel, "Created new CsvSerializer");
			break;
		default:
			throw new IllegalArgumentException("Unknown serialization strategy " + strategy);
		}
		return serializer;
	}

	/*
	 * Check each object's attributes for compliance (must not be a complex type).
	 */
	private void validatePojos(Object[] pojos) {
		if(pojos.length == 0){
			throw new IllegalArgumentException("Nothing to serialize"); 
		}
		
		for(Object pojo : pojos){
			String className = pojo.getClass().getSimpleName();
			for(Field attribute : pojo.getClass().getDeclaredFields()){
				validateAttribute(attribute, className);
			}
		}
	}

	/*
	 * Checks if a POJO's attribute is not a complex type
	 */
	private void validateAttribute(Field attribute, String className) {
		Class<?> type = attribute.getType();
		if(type.isPrimitive() || type.isEnum() || String.class.isAssignableFrom(type)){
			return;
		}

		// damn library restrictions! ;)
		if(type.equals(Boolean.class)
			|| type.equals(Character.class)
			|| type.equals(Byte.class)
			|| type.equals(Short.class)
			|| type.equals(Integer.class)
			|| type.equals(Long.class)
			|| type.equals(Float.class)
			|| type.equals(Double.class)){
			return;
		}

		throw new IllegalArgumentException("Field " + attribute.getName() + " of class " + className 
								+ " is not a primitive, enum, string or wrapper class");
	}
}
