package org.biomart.preprocess;

import java.io.IOException;
import java.io.OutputStream;

import org.biomart.common.exceptions.TechnicalException;
import org.biomart.preprocess.utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


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
	
	protected static Document keepFilterListNameOnly(String xml) {
		return keepFilterListNameOnly(Utils.parseXML(xml));
	}
	
	protected static Document keepFilterListNameOnly(Document d) {
		return filterStrip(d, 0);
	}
	
	protected Document keepFilterNameOnly(String xml) {
		return keepFilterNameOnly(Utils.parseXML(xml));
	}
	
	protected Document keepFilterNameOnly(Document d) {
		return filterStrip(d, 1);
	}
	
	/**
	 * Removes from filter names either the filter name of the filter-list
	 * name.
	 * @param d
	 * @param token - the index of the token to keep. 0 for filter name;
	 * 1 for filter-list name.
	 * @return a new Document instance formatted as above.
	 */
	protected static Document filterStrip(Document d, int token) {
		NodeList nl = d.getDocumentElement().getElementsByTagName("Filter");
		Element f = null;
		String name = null;
		String[] tokens = null;
		for (int i = 0, len = nl.getLength(); i < len; ++i) {
			f = (Element)nl.item(i);
			name = f.getAttribute("name");
			// if there are not the right amount of tokens, throw...
			tokens = name.split("#");
			if (tokens.length > 0)
				f.setAttribute("name", tokens[token]);
		}
		return d;
	}	
}
