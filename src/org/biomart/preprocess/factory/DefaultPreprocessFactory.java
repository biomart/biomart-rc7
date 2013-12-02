package org.biomart.preprocess.factory;

import org.biomart.common.resources.Log;
import org.biomart.preprocess.DefaultPreprocess;
import org.biomart.preprocess.Preprocess;
import org.biomart.preprocess.PreprocessParameters;

public class DefaultPreprocessFactory implements PreprocessFactory {

	@Override
	public Preprocess newPreprocess(PreprocessParameters pp) {
		Log.debug(DefaultPreprocessFactory.class.getName() + " invoked");
		return new DefaultPreprocess(pp);
	}

}
