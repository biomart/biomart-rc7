package org.biomart.preprocess.utils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.biomart.common.exceptions.ValidationException;
import org.biomart.common.resources.Log;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Utils {
	
	/**
	 * Splits a document into as many documents as the number of elType
	 * elements present within it. Each document includes just one of the
	 * elType elements.
	 * 
	 * @param doc - original document
	 * @param elType - element type e.g. Filter, Attribute.
	 * @return Array of documents where each one of them has only one
	 * elType element.
	 */
	public static Document[] split(Document doc, String elType) {
		Log.debug("Utils#split invoked");
		Node[] els = removeElement(doc, elType);
		Document[] docs = null;

		int len = els.length;
		docs = new Document[len];
		Document q = null;
		Node dataset = null;

		for (int x = 0; x < len; ++x) {
			q = copy(doc);
			dataset = q.getElementsByTagName("Dataset").item(0);
			dataset.appendChild(q.importNode(els[x], true));
			docs[x] = q;
		}
		
		return docs;
	}
	
	/**
	 * Removes all the instances of an element.
	 * 
	 * @param doc
	 * @param elType
	 * @return An array of removed elements.
	 */
	public static Node[] removeElement(Document doc, String elType) {
		Log.debug("Utils#remove doc = ...");
		Log.debug("Utils#remove "+ toXML(doc));
		
		Element root = doc.getDocumentElement();
		NodeList rels = root.getElementsByTagName(elType);
		int len = rels.getLength(), z = 0;
		Node[] na = new Node[len];
		
		for (; z < len; ++z) 
			na[z] = rels.item(z);
		
		for (Node n : na) {
			n.getParentNode().removeChild(n);
		}
		
		return na;
	}
	
	public static Document copy(Document doc) {
		return (Document)doc.cloneNode(true);
	}
	
	public static Document parseXML(String xml) {
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
	
	public static String toXML(Document d) {
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
	
	public static String getProcessor(Document doc) {
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
