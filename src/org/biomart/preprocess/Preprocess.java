package org.biomart.preprocess;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.biomart.common.exceptions.TechnicalException;
import org.biomart.common.exceptions.ValidationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public abstract class Preprocess {
	
	protected OutputStream out;
	protected PreprocessParameters params;
	
	public Preprocess(PreprocessParameters params) {
		this.params = params;
	}
	
	public abstract String getContentType();
	
	public abstract void run(OutputStream out) throws TechnicalException, IOException;
	
	static public Document parseXML(String xml) {
		InputStream in = null;
		DocumentBuilder builder = null;
		Document doc = null;
		try {
			in = new ByteArrayInputStream(xml.getBytes("UTF-8"));
			builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			doc = builder.parse(in);
			doc.normalize();
		} catch (Exception e) {
            throw new ValidationException(e.getMessage(), e);
		}
		
		return doc;
	}
	
	static public String toXML(Document d) {
		try {
			TransformerFactory tf = TransformerFactory.newInstance();
			Transformer transformer = tf.newTransformer();
			transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			StringWriter writer = new StringWriter();
			transformer.transform(new DOMSource(d), new StreamResult(writer));
			return writer.getBuffer().toString().replaceAll("\n|\r", "");
		} catch (Exception e) {
            throw new ValidationException(e.getMessage(), e);
		}
	}
	
	static public String getProcessor(Document doc) {
		Element root = doc.getDocumentElement();
		String processor = root.getAttribute("processor");
		
        if (processor.isEmpty()) {
        		NodeList nl = root.getElementsByTagName("Processor");
        		if (nl.getLength() != 0) 
            		processor = ((Element) nl.item(0)).getAttribute("name");
        }
        
        return processor;
	}
	
}
