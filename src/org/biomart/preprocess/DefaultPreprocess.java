package org.biomart.preprocess;

import java.io.IOException;
import java.io.OutputStream;

import org.biomart.common.exceptions.TechnicalException;
import org.biomart.common.resources.Log;
import org.biomart.preprocess.utils.Utils;
import org.biomart.queryEngine.QueryController;

public class DefaultPreprocess extends Preprocess {

	QueryController qc;
	
	public DefaultPreprocess(PreprocessParameters params) {
		super(params);
		
		Log.debug("DefaultPreprocess::DefaultPreprocess invoked");
		
		qc = new QueryController(Utils.toXML(keepFilterListNameOnly(params.getXML())),
								params.getRegistry(),
								params.getUser(),
								params.getMimes(),
								params.getCountQuery());
	}

	@Override
	public String getContentType() {
		return qc.getContentType();
	}

	@Override
	public void run(OutputStream out) throws TechnicalException, IOException{
		Log.debug("DefaultPreprocess::run invoked");

		qc.runQuery(out);
	}

	
}
