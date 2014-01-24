package org.biomart.dino.tests;

import static org.junit.Assert.*;

import java.io.File;

import org.biomart.dino.command.HypgCommand;
import org.junit.Before;
import org.junit.Test;

public class HypgCommandTest {

	HypgCommand cmd;
	final File ann = new File("ann/path"),
			bk = new File("background/path"),
			sets = new File("sets/path"),
			bin = new File("bin/path");
	String cutoff = "0.4";
	
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
		assertTrue(r.contains("-g "+bk.getPath()));
		assertTrue(r.contains("-s "+sets.getPath()));
		assertTrue(r.contains("-a "+ann.getPath()));
		assertTrue(r.contains("-c "+cutoff));
	}

}
