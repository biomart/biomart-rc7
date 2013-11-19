package org.biomart.queryEngine;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang.ArrayUtils;
import org.biomart.common.exceptions.TechnicalException;
import org.biomart.common.exceptions.ValidationException;
import org.biomart.objects.objects.MartRegistry;
import org.biomart.preprocess.Preprocess;
import org.biomart.preprocess.PreprocessParameters;
import org.biomart.preprocess.PreprocessRegistry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class QueryControllerProxy {

	private Preprocess pp = null;
	private PreprocessParameters params = null;
	private static final String DEFAULT_PREPROCESS = "defaultpreprocess";
	
    public QueryControllerProxy(String xml,
    								final MartRegistry registryObj,
    								String user, 
    								String[] mimes, 
    								boolean isCountQuery) {
    	
    		PreprocessRegistry.install();
    		
    		params = new PreprocessParameters(xml,
    										 registryObj,
    										 user,
    										 mimes,
    										 isCountQuery);
    		
    		String ppName = getProcessor(parseQuery(xml));
    		
    		if ((pp = match(ppName)) == null)
    			pp = match(QueryControllerProxy.DEFAULT_PREPROCESS);
    		
    		
    		if (pp == null)
    			throw new ValidationException("No default preprocessor found"); 		
    }

    public QueryControllerProxy(String xml,
    								final MartRegistry registryObj, 
    								String user, 
    								boolean isCountQuery) {
    		this(xml,
    			registryObj,
    			user, 
    			ArrayUtils.EMPTY_STRING_ARRAY,
    			isCountQuery);
    }
    
    public void runQuery(OutputStream outputHandle) throws TechnicalException, IOException {
    		pp.run(outputHandle);
    }
    
    public String getContentType() {
    		return pp.getContentType();
    }
    
    private Document parseQuery(String xml) {
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
    
    private String getProcessor(Document doc) {
		Element root = doc.getDocumentElement();
		String processor = root.getAttribute("processor");
		
        if (processor.isEmpty()) {
        		NodeList nl = root.getElementsByTagName("Processor");
        		if (nl.getLength() != 0) 
            		processor = ((Element) nl.item(0)).getAttribute("name");
        }
        
        return processor;
	}
    
    private Preprocess match(String name) {
    		Class klass = PreprocessRegistry.get(name);
    		
    		if (klass == null) return null;
    		
    		return getPreprocessInstance(klass);
    }
    
    private Preprocess getPreprocessInstance(Class klass) {
    		Preprocess p = null;
    		
    		try {
    			Constructor<Preprocess> ctor = 
    				klass.getDeclaredConstructor(PreprocessParameters.class);
    			p = (Preprocess)ctor.newInstance(params);
    		} catch (Exception e) {
                throw new ValidationException(e.getMessage(), e);
    		}
    		
    		return p;
    }
    
    
}
