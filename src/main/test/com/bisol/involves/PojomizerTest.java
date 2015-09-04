package com.bisol.involves;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Test;

public class PojomizerTest {
	private final Logger logger = Logger.getLogger(getClass().getName());

	@Test
	public void testValidPojo()
	throws IOException, ReflectiveOperationException {
		logger.info("-------------------------------------------------------------------------\n"
					+ "testValidPojo\n"
					+ "-------------------------------------------------------------------------");
		File targetFile = new File("target/f.csv");
		OutputStream outStream = new Pojomizer().serializePojosToFile(targetFile, new ValidPojo());
		outStream.close();
		
		BufferedReader bufferedReader = new BufferedReader(new FileReader(targetFile));
		String line = bufferedReader.readLine();
		Assert.assertEquals("field1,fieldC,fieldS", line);
		
		line = bufferedReader.readLine();
		Assert.assertEquals("1,c,S", line);
		
		bufferedReader.close();
	}

	@Test
	public void testValidPojos()
	throws IOException, ReflectiveOperationException {
		logger.info("-------------------------------------------------------------------------\n"
				+ "testValidPojos\n"
				+ "-------------------------------------------------------------------------");
		File targetFile = new File("target/f.csv");
		Object[] pojos = {new ValidPojo(), new ValidPojo2(), new ValidPojo3(), new ValidPojo4()};
		OutputStream outStream = new Pojomizer().serializePojosToFile(targetFile, pojos);
		outStream.close();
		
		BufferedReader bufferedReader = new BufferedReader(new FileReader(targetFile));
		
		String line = bufferedReader.readLine();
		Assert.assertEquals("field1,fieldC,fieldS,fieldE,fieldB,attr1,attr2", line);
		
		line = bufferedReader.readLine();
		Assert.assertEquals("1,c,S,,,,", line);
		
		line = bufferedReader.readLine();
		Assert.assertEquals("1,2.4,S,constant1,true,,", line);
		
		line = bufferedReader.readLine();
		Assert.assertEquals(",teste,,,,,", line);
		
		line = bufferedReader.readLine();
		Assert.assertEquals(",,,,,teste,teste 2", line);
		
		bufferedReader.close();
	}

	@Test
	public void testValidCollection()
	throws IOException, ReflectiveOperationException {
		logger.info("-------------------------------------------------------------------------\n"
				+ "testValidCollection\n"
				+ "-------------------------------------------------------------------------");
		File targetFile = new File("target/f.csv");
		Collection<Object> pojos = new ArrayList<>();
		pojos.add(new ValidPojo());
		pojos.add(new ValidPojo2());
		pojos.add(new ValidPojo3());
		pojos.add(new ValidPojo4());
		OutputStream outStream = new Pojomizer().serializePojosToFile(targetFile, pojos);
		outStream.close();
		
		BufferedReader bufferedReader = new BufferedReader(new FileReader(targetFile));
		
		String line = bufferedReader.readLine();
		Assert.assertEquals("field1,fieldC,fieldS,fieldE,fieldB,attr1,attr2", line);
		
		line = bufferedReader.readLine();
		Assert.assertEquals("1,c,S,,,,", line);
		
		line = bufferedReader.readLine();
		Assert.assertEquals("1,2.4,S,constant1,true,,", line);
		
		line = bufferedReader.readLine();
		Assert.assertEquals(",teste,,,,,", line);
		
		line = bufferedReader.readLine();
		Assert.assertEquals(",,,,,teste,teste 2", line);
		
		bufferedReader.close();
	}

	@Test(expected=java.lang.IllegalArgumentException.class)
	public void testInvalidPojos()
	throws IOException, ReflectiveOperationException {
		logger.info("-------------------------------------------------------------------------\n"
				+ "testInvalidPojos\n"
				+ "-------------------------------------------------------------------------");
		File targetFile = new File("target/f.csv");
		OutputStream outStream = new Pojomizer().serializePojosToFile(targetFile, new ValidPojo2(), new InvalidPojo());
		outStream.close();
	}
}

class ValidPojo {
	private Integer field1 = 1;
	char fieldC = 'c';
	public String fieldS = "S";
}

class ValidPojo2 extends ValidPojo {
	public EnumTest		fieldE = EnumTest.constant1;
	public boolean		fieldB = true;
	public float		fieldC = 2.4f;
}

class ValidPojo3 {
	String fieldC = "teste";
	public EnumTest fieldE = null;
}

class ValidPojo4 {
	String attr1 = "teste";
	String attr2 = "teste 2";
}

enum EnumTest {
	constant1,
	constant2;
}

class InvalidPojo extends ValidPojo {
	public ValidPojo2 pojo2 = new ValidPojo2();
}