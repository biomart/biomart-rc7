package org.biomart.preprocess;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.biomart.common.exceptions.TechnicalException;
import org.biomart.common.resources.Log;
import org.biomart.preprocess.utils.Utils;
import org.biomart.queryEngine.QueryController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class InputFormatTranslator extends Preprocess {
	
	Document d;
	String translatorAttribute;
	
	public InputFormatTranslator(PreprocessParameters pp) {
		this(pp, Utils.parseXML(pp.getXML()), "external_gene_id");
	}
	
	public InputFormatTranslator(PreprocessParameters pp,
			Document doc, String attribute) {
		super(pp);
		Log.debug(this.getClass().getName() + " new instance");
		Log.debug(this.getClass().getName() + "#InputFormatTranslator original query: "+ Utils.toXML(doc));
		
		d = doc;
		translatorAttribute = attribute;
		Utils.removeElement(d, "Attribute");
		
		Element root = d.getDocumentElement(), 
				attr = d.createElement("Attribute"), 
				dataset = (Element)
						root.getElementsByTagName("Dataset")
						.item(0);
		
		attr.setAttribute("name", translatorAttribute);
		dataset.appendChild(attr);
		// plain/text results
		root.setAttribute("processor", "TSVX");
	}
	
	@Override
	public String getContentType() {
		return "plain/text";
	}

	@Override
	public void run(OutputStream out) throws TechnicalException, IOException {
		Log.debug(this.getClass().getName() + " translating into ensemble IDs ...");
		Log.debug(this.getClass().getName() + "#run query: "+ Utils.toXML(d));
		new QueryController(Utils.toXML(d), // Note
				params.getRegistry(),
				params.getUser(),
				params.getMimes(),
				params.getCountQuery()).runQuery(new DropHeaderStream(out));
		out.flush();
		Log.debug(this.getClass().getName() + " translation into ensemble IDs finished");
	}
	
	class DropHeaderStream extends FilterOutputStream {
		boolean header = false;
		
		public DropHeaderStream(OutputStream o) {
			super(o);
		}
		
		@Override
		public void write(int b) throws IOException {
			if (!header) {
				header = true;
				Log.debug(this.getClass().getName() +" ignoring header...");
				return;
			}
			//Log.debug(this.getClass().getName() +" printing byte: "+ Integer.toString(b));
			out.write(b);
		}
		
		@Override
		public void write(byte[] b) throws IOException {
			if (!header) {
				header = true;
				Log.debug(this.getClass().getName() +" ignoring header...");
				return;
			}
			//Log.debug(this.getClass().getName() +" printing "+ new String(b));
			out.write(b);
		}
		
		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			if (!header) {
				header = true;
				Log.debug(this.getClass().getName() +" ignoring header...");
				return;
			}
			//Log.debug(this.getClass().getName() +" printing "+ new String(b));
			out.write(b, off, len);
		}
	}
}
