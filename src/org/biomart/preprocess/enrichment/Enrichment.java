package org.biomart.preprocess.enrichment;


import java.io.IOException;
import java.io.OutputStream;

import org.biomart.common.exceptions.TechnicalException;
import org.biomart.preprocess.Preprocess;
import org.biomart.preprocess.PreprocessParameters;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class Enrichment extends Preprocess {
		
	public Enrichment(PreprocessParameters params) {
		super(params);
	}

	@Override
	public abstract String getContentType();

	@Override
	public void run(OutputStream out) throws TechnicalException, IOException {
		runEnrichment();
	}
	
	public abstract void runEnrichment();
	
	public String getFilterContent(Document doc, String filter) {
		NodeList fs = doc.getDocumentElement().getElementsByTagName("Filter");
		String name = null;
		Element e = null;
		for (int i = 0, len = fs.getLength(); i < len; ++i) {
			e = (Element)fs.item(i);
			name = e.getAttribute("name");
			if (name.equalsIgnoreCase(filter)) {
				return e.getAttribute("value"); 
			}
		}
		
		return "";
	}

}

