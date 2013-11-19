package org.biomart.preprocess;

import java.io.IOException;
import java.io.OutputStream;

import org.biomart.common.exceptions.TechnicalException;
import org.biomart.queryEngine.QueryController;

public class DefaultPreprocess extends Preprocess {

	QueryController qc;
	
	public DefaultPreprocess(PreprocessParameters params) {
		super(params);
		
		qc = new QueryController(params.getXML(),
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
		qc.runQuery(out);
	}

	
}
