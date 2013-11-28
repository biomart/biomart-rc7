package org.biomart.preprocess.enrichment.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.biomart.preprocess.enrichment.Configuration;
import org.junit.Ignore;
import org.junit.Test;


public class ConfigurationTest {
	
	Configuration c;
	
	@Test
	public void defaultConfigPath () {
		try {
			c = new Configuration();
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
		assertEquals(
				"the default configuration path is the absolute path of the enrichment package plus the file name",
				System.getProperty("user.dir") + "/preprocesses/enrichment/enrichment.properties", c.getConfigPath());
	}
	
	@Test
	public void biomartdirConfigPath() {
		// 			fail("Unable to craete read the configuration");
		c = new Configuration("/biomart");
		assertEquals(
				"the configuration path starts from biomart.basedir property value",
				"/biomart/enrichment.properties", c.getConfigPath());
	}
	
	
//	@Test(expected = NullPointerException.class)
//	public void readPropertyException() {
//		c.getProperty("hygtpath");
//	}
	@Test
	public void readProperty() {
		String d = System.getProperty("file.separator");
		c = new Configuration(join(new String[] {
				System.getProperty("user.dir"),
				"src", "org", "biomart", "preprocess", "enrichment", "test"
		}, d));
		try {
			c.load();
		} catch (IOException e) {
			fail();
			e.printStackTrace();
		}
		assertEquals("read the proper property value", "/a/path.c", c.getProperty("hygtpath"));
	}
	
	public String join(String[] ss, String d) {
		String s = ss[0];
		for (int i = 1; i < ss.length; ++i) {
			s += d + ss[i];
		}
		return s;
	}

}
