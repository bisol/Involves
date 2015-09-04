package com.bisol.involves;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

/**
 * Base interface for Pojomizer's serialization strategy implementations. 
 * 
 * @author bisol
 */
public interface PojoSerializer {
	/**
	 * Target Character Set for serialization. 
	 * @param charset
	 */
	void setCharset(String charset);

	/**
	 * POJOs to serialize. 
	 * It is up to the implementation to decide if objects of different classes can be serialized together.
	 * @param pojos An array of objects which each attribute is a simple type (Strings, enums, primitives and primitive wrappers)
	 */
	void setPojos(Object[] pojos);
	
	/**
	 * Specifies the line terminator to use.
	 * Implementations may ignore it or limit selection.  
	 */
	void setLineTerminator(String lineTerminator);
	
	/**
	 * Uses reflection to access each POJO's attributes and outputs them to the supplied stream in a implementation specific way.
	 * @param os target for serialization output 
	 * @return the received OutputStream
	 * @throws UnsupportedEncodingException if the specified charset in not available.
	 * @throws IOException if there is any problem writing to the supplied stream.
	 * @throws ReflectiveOperationException if there is a problem accessing a POJO's attributes
	 */
	OutputStream serializePojosToStream(OutputStream os)
			throws UnsupportedEncodingException, IOException, ReflectiveOperationException;
}
