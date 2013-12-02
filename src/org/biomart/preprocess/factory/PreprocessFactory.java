package org.biomart.preprocess.factory;

import org.biomart.preprocess.Preprocess;
import org.biomart.preprocess.PreprocessParameters;

public interface PreprocessFactory {
	
	public Preprocess newPreprocess(PreprocessParameters pp);

}
