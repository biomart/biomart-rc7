package org.biomart.dino.tests;

import static org.junit.Assert.*;

import org.biomart.dino.dinos.Dino;
import org.biomart.dino.dinos.EnrichmentDino;
import org.junit.Before;
import org.junit.Test;

public class EnrichmentDinoTest {

	EnrichmentDino dino;
	
	@Before
	public void setUp() throws Exception {
		dino = new EnrichmentDino();
	}

	@Test
	public void test() {
		assertTrue("EnrichmentDino implements Dino", dino instanceof Dino);
	}

}
