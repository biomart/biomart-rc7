package org.biomart.dino.command;

import java.io.File;
import java.io.IOException;

import org.biomart.common.resources.Log;

public abstract class ShellRunner implements CommandRunner {

	protected ShellCommand cmd;
	protected int nIter = 0, maxWait = 15000, errorResult = -42, waitTime = 200;
	protected String dir = System.getProperty("java.io.tmpdir");
	
	public ShellRunner setCmd(ShellCommand cmd) {
		this.cmd = cmd;
		return this;
	}
	
	public CommandRunner run() throws IOException, InterruptedException {
		
		Runtime rt = Runtime.getRuntime();
		Process pr = null;
		String command = cmd.build();
		
		try {
			pr = rt.exec(command, null, new File(dir));
			
			while(true) {
				Thread.sleep(waitTime);
				try {
					// If it hasn't finished yet, it throws an exception.
					errorResult = pr.exitValue();
					// If it reaches this point, it has finished.
					break;
				} catch(IllegalThreadStateException e) {
					if (++nIter < maxWait) continue;
					break;
				}
			}
			
			if (errorResult != 0)
				throw new IOException("Process didn't terminate or retured a value of error: "
					+ errorResult);
		} catch (IOException e) {
			Log.error(e);
			throw e;
		} catch (InterruptedException e) {
			Log.error(this.getClass().getName() + "#run() runner has been interrupted", e);
			throw e;
		}
		
		return this;
	}
}
