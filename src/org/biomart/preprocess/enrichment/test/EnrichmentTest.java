package org.biomart.preprocess.enrichment.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import org.biomart.preprocess.Preprocess;
import org.biomart.preprocess.PreprocessParameters;
import org.biomart.preprocess.enrichment.Enrichment;
import org.biomart.preprocess.enrichment.HGTEnrichment;
import org.biomart.preprocess.utils.Utils;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;

public class EnrichmentTest {
	HGTEnrichment e;
	Document query;
	
	@Before
	public void setUp() {
		String xml = null;
		String d = System.getProperty("file.separator");
		String path = join(new String[] {
				System.getProperty("user.dir"),
				"src", "org", "biomart", "preprocess", "enrichment", "test",
				"query.xml"
		}, d);
		
		File f = new File(path);
		FileInputStream fs;
		byte[] data = new byte[(int)f.length()];
		try {
			fs = new FileInputStream(f);
			fs.read(data);
			fs.close();
			xml = new String(data, "UTF-8");
		} catch (IOException e) {
			fail("unable to read query file "+ f.getPath());
			e.printStackTrace();
		}
		
		query = Utils.parseXML(xml);
		PreprocessParameters pp = new PreprocessParameters(xml, null, null, null, false);
		e = new HGTEnrichment(pp);
	}
	
	@Test
	public void getFilterContentTest() {
		assertEquals("foo", this.e.getFilterContent(query, "background"));
		assertEquals("bar", this.e.getFilterContent(query, "sets"));
	}
	
	@Test
	public void makeSetTest() {
		assertEquals("<set\na\nb\n>set\n", e.makeSet("<set", "a,b", ">set"));
	}
	
	@Test
	public void makeBackgroundTest() {
		assertEquals("a\nb\n", e.makeBackground("a,b"));
	}
	
	public String join(String[] ss, String d) {
		String s = ss[0];
		for (int i = 1; i < ss.length; ++i) {
			s += d + ss[i];
		}
		return s;
	}

}
