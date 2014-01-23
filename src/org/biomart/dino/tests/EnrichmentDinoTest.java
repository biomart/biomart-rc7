package org.biomart.dino.tests;

import static org.junit.Assert.*;

import org.biomart.dino.command.Command;
import org.biomart.dino.command.CommandRunner;
import org.biomart.dino.command.HypgCommand;
import org.biomart.dino.dinos.Dino;
import org.biomart.dino.dinos.EnrichmentDino;
import org.junit.Before;
import org.junit.Test;


public class EnrichmentDinoTest {

	EnrichmentDino dino;
	Command cmd;
	CommandRunner cmdRunner;
	
	@Before
	public void setUp() throws Exception {
		cmd = new HypgCommand();
		cmdRunner = new HgmcRunner();
	}

	@Test
	public void test() {
		assertTrue("EnrichmentDino implements Dino", dino instanceof Dino);
	}

}
