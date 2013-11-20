package org.biomart.preprocess;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import org.biomart.preprocess.Preprocess;
import org.biomart.api.BioMartApiException;
import org.biomart.common.exceptions.TechnicalException;
import org.biomart.common.resources.Log;
import org.biomart.queryEngine.QueryController;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class QuerySplit extends Preprocess {

	Document doc;
	Document[] queries;
	Node[] attrs;
	QueryController currentQC;
	
	public QuerySplit(PreprocessParameters params) {
		this(params, parseXML(params.getXML()));
	}
	
	public QuerySplit(PreprocessParameters p, Document doc) {
		super(p);
		Log.debug("QuerySplit::QuerySplit invoked");
		
		this.doc = doc;
		
		makeQueries();
	}

	@Override
	public String getContentType() {
		// Just create a new controller to answer this would be too much
		// expensive. 
		if (currentQC == null) {
			currentQC = newController(toXML(queries[0]), params);
		}
		
		return currentQC.getContentType();
	}

	@Override
	public void run(OutputStream out) throws TechnicalException, IOException {
		Log.debug("QuerySplit#run invoked");
		
		this.out = out;

		// TODO: a better way to handle the answering to getContentType
		// and running of queries
		if (currentQC != null) {
			currentQC.runQuery(out);
			queries = Arrays.copyOfRange(queries, 1, queries.length);
		}
		
		for (Document q : queries) {
			String str = toXML(q);
			Log.debug("QuerySplit#run subquery :"+ str);
			currentQC = newController(str, params);
			currentQC.runQuery(out);
		}
	}
	
	protected Document[] makeQueries() {
		Log.debug("QuerySplit#makeQueries invoked");
		if (queries == null) {
			String processor = getProcessor(doc);

			removeProcessorAttribute(doc);
			
			attrs = removeAttrs(doc);
			
			int len = attrs.length;
			queries = new Document[len];
			Document q = null;
			Node dataset = null;
			
			for (int x = 0; x < len; ++x) {
				q = copy(doc);
				dataset = q.getElementsByTagName("Dataset").item(0);
				dataset.appendChild(q.importNode(attrs[x], true));
				setProcessorParam(q, processor);
				queries[x] = q;
			}
		}
		
		return queries;
	}
	
	protected void removeProcessorAttribute(Document doc) {
		doc.getDocumentElement().removeAttribute("processor");
	}
	
	protected Node[] removeAttrs(Document doc) {
		Log.debug("Processor#removeAttrs doc = ...");
		Log.debug("Processor#removeAttrs "+ toXML(doc));
		
		Element root = doc.getDocumentElement();
		Element dataset = (Element)root.getElementsByTagName("Dataset").item(0);
		NodeList attrs = dataset.getElementsByTagName("Attribute");
		int len = attrs.getLength(), z = 0;
		Node[] na = new Node[len];
		
		for (; z < len; ++z) 
			na[z] = attrs.item(z);
		
		for (Node n : na) {
			dataset.removeChild(n);
		}
		
		return na;
	}
	
	protected Document copy(Document doc) {
		return (Document)doc.cloneNode(true);
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
            Log.error("QuerySplit#newController Error during querying", e);
            throw new BioMartApiException(e.getMessage());
        }
	}

}
