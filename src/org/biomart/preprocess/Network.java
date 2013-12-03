package org.biomart.preprocess;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import org.biomart.preprocess.Preprocess;
import org.biomart.preprocess.utils.Utils;
import org.biomart.api.BioMartApiException;
import org.biomart.common.exceptions.TechnicalException;
import org.biomart.common.resources.Log;
import org.biomart.queryEngine.QueryController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class Network extends Preprocess {

	Document doc;
	Document[] queries;
	Node[] attrs;
	QueryController currentQC;
	
	public Network(PreprocessParameters params) {
		super(params);
		Log.debug("Network::Network invoked");
		
		this.doc = keepFilterListNameOnly(params.getXML());
		
		makeQueries();
	}

	@Override
	public String getContentType() {
		// Just create a new controller to answer this would be too much
		// expensive. 
		if (currentQC == null) {
			currentQC = newController(Utils.toXML(queries[0]), params);
		}
		
		return currentQC.getContentType();
	}

	@Override
	public void run(OutputStream out) throws TechnicalException, IOException {
		Log.debug("Network#run invoked");
		
		this.out = out;

		// TODO: a better way to handle the answering to getContentType
		// and running of queries
		if (currentQC != null) {
			currentQC.runQuery(out);
			queries = Arrays.copyOfRange(queries, 1, queries.length);
		}
		
		for (Document q : queries) {
			String str = Utils.toXML(q);
			Log.debug("Network#run subquery :"+ str);
			currentQC = newController(str, params);
			currentQC.runQuery(out);
		}
	}
	
	protected void makeQueries() {
		Log.debug("Network#makeQueries invoked");
		String processor = Utils.getProcessor(doc);

		removeProcessorAttribute(doc);

		queries = Utils.split(doc, "Attribute");

		for (int x = 0; x < queries.length; ++x) {
			setProcessorParam(queries[x], processor);
		}
	}
	
	protected void removeProcessorAttribute(Document doc) {
		doc.getDocumentElement().removeAttribute("processor");
	}
	
	protected void setProcessorParam(Document q, String processor) {
		Element procEl = q.createElement("Processor");
		procEl.setAttribute("name", processor);
		
		Element paramEl = q.createElement("Parameter");
		paramEl.setAttribute("name", "nqueries");
		paramEl.setAttribute("value", Integer.toString(queries.length));
		
		procEl.appendChild(paramEl);
		// Is this the right place to append?
		q.getDocumentElement().appendChild(procEl);
	}
	
	protected QueryController newController(String xml, PreprocessParameters p) {
		try {
            return new QueryController(xml != null ? xml : p.getXML(), 
            						p.getRegistry(), 
            						p.getUser(), 
            						p.getMimes(),
            						p.getCountQuery());
        } catch (Exception e) {
            Log.error("Network#newController Error during querying", e);
            throw new BioMartApiException(e.getMessage());
        }
	}

}
