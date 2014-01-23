package org.biomart.dino.command;

import java.io.File;

public class HypgRunner extends ShellRunner {

	@Override
	public Object getResults() {
		// With the current hypg.exe program, the output will be in the
		// hypg.pv file in the folder the bin has been run.
		
		return new File(this.dir, "hypg.pv");
	}

}
