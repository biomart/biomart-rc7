package org.biomart.dino;

public interface Command {

	/**
	 * It returns the command to run in string format.
	 * @return the command to run on command line.
	 */
	String build();
}
