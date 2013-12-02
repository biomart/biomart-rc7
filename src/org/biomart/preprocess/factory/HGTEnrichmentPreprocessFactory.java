package org.biomart.preprocess.factory;

import org.biomart.common.resources.Log;
import org.biomart.preprocess.Preprocess;
import org.biomart.preprocess.PreprocessParameters;
import org.biomart.preprocess.enrichment.HGTEnrichment;

public class HGTEnrichmentPreprocessFactory implements PreprocessFactory {

	@Override
	public Preprocess newPreprocess(PreprocessParameters pp) {
		Log.debug(HGTEnrichmentPreprocessFactory.class.getName() + " invoked");
		return new HGTEnrichment(pp);
	}

}
