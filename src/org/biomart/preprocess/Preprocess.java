package org.biomart.preprocess;

import java.io.IOException;
import java.io.OutputStream;

import org.biomart.common.exceptions.TechnicalException;


public abstract class Preprocess {
	
	protected OutputStream out;
	protected PreprocessParameters params;
	
	public Preprocess() {}
	
	public Preprocess(PreprocessParameters params) {
		this.params = params;
	}
	
	public abstract String getContentType();
	
	public abstract void run(OutputStream out) throws TechnicalException, IOException;
	
	public PreprocessParameters getParameters() {
		return params;
	}
}
