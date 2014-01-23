package org.biomart.dino.tests;

import static org.junit.Assert.*;

import org.biomart.dino.command.HypgCommand;
import org.junit.Before;
import org.junit.Test;

public class HypgCommandTest {

	HypgCommand cmd;
	final String ann = "ann/path",
			bk = "background/path",
			sets = "sets/path",
			cutoff = "0.4",
			bin = "bin/path";
	
	@Before
	public void setUp() {
		cmd = new HypgCommand();
		cmd.setAnnotations(ann)
			.setBackground(bk)
			.setSets(sets)
			.setCutoff(cutoff)
			.setCmdBinPath(bin);
	}
	
	@Test
	public void test() {
		String r = cmd.build();
		assertTrue(r.contains("-g "+bk));
		assertTrue(r.contains("-s "+sets));
		assertTrue(r.contains("-a "+ann));
		assertTrue(r.contains("-c "+cutoff));
	}

}
