package org.biomart.preprocess.factory;

import org.biomart.common.resources.Log;
import org.biomart.preprocess.Preprocess;
import org.biomart.preprocess.PreprocessParameters;
import org.biomart.preprocess.QuerySplit;

public class NetworkPreprocessFactory implements PreprocessFactory {

	@Override
	public Preprocess newPreprocess(PreprocessParameters pp) {
		Log.debug(NetworkPreprocessFactory.class.getName() + " invoked");
		return new QuerySplit(pp);
	}

}
