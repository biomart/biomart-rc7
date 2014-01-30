package org.biomart.dino.tests;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.biomart.dino.configreader.AttributesConfig;
import org.junit.Before;
import org.junit.Test;

public class AttributesConfigTest {

	AttributesConfig cfg;
	static final String sep = AttributesConfig.sep,
			dirPath = StringUtils.join(new String [] {
					System.getProperty("user.dir"),
					"test",
					"org",
					"biomart",
					"dino",
					"tests",
					"fixtures"
			}, sep),
			fileName = "testattributes.properties"; 
	Map<String, String> attrs, m;
	
	@Before
	public void setUp() {
		// Gene Ontology = Gene Ontology (GO), Kegg=KEGG
		attrs = new HashMap<String, String>();
		attrs.put("Gene Ontology", "Gene Ontology (GO)");
		attrs.put("Kegg", "KEGG");
		m = null;
	}
	
	@Test
	public void readFailTest() {
		cfg = new AttributesConfig(dirPath);
		m = cfg.read("i don't exist");
		assertTrue(m.isEmpty());
	}
	
	@Test
	public void readTest() {
		cfg = new AttributesConfig(dirPath);
		m = cfg.read(fileName);

		assertFalse(m.isEmpty());
		Iterator it = m.entrySet().iterator();
		while(it.hasNext()) {
			@SuppressWarnings("unchecked")
			Map.Entry<String, String> e = (Entry<String, String>) it.next();
			assertEquals(attrs.get(e.getKey()), e.getValue());
		}
	}

}
