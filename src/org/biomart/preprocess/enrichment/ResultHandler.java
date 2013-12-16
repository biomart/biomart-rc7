package org.biomart.preprocess.enrichment;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.biomart.common.exceptions.TechnicalException;
import org.biomart.common.resources.Log;
import org.biomart.preprocess.DefaultPreprocess;
import org.biomart.preprocess.PreprocessParameters;
import org.biomart.preprocess.utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class ResultHandler {
	
	EnrichmentResultParser parser;
	PreprocessParameters pp;
	Document doc; 
	
	public ResultHandler(PreprocessParameters pp, 
			EnrichmentResultParser parser, Document doc) {
		Log.debug(this.getClass().getName() + "::ResultHandler doc: "+ Utils.toXML(doc));
		this.doc = doc;
		this.pp = pp;
		this.parser = parser;
	}
	
	
	public String[][] getResults() throws IOException {
		Log.debug(this.getClass().getName() + "#getResults invoked ");
		
		parser.retrieveResults();
		SortedMap<Double, String> ann = parser.getAnnotations();
		Map<String, String> desc, bonPv;
		Map<String, List<String>> genes;
		Element attr = (Element) doc.getElementsByTagName("Attribute")
				.item(0);
		String query = selectQuery(attr.getAttribute("name"),
				ann.values().toArray(new String[0]));
		try {
			desc = issueQuery(query);
		} catch (TechnicalException e1) {
			Log.error(this.getClass().getName() + "#getResults ", e1);
			return null;
		}
		genes = parser.getGenes();
		bonPv = parser.getBonPv();
		
		String[][] r = new String[ann.size()][];		
		Set<Map.Entry<Double, String>> set = ann.entrySet();
		Iterator<Map.Entry<Double, String>> it = set.iterator();
		Map.Entry<Double, String> e;
		String k = null;
		int idx = 0;
		
		while(it.hasNext()) {
			e = it.next(); k = e.getValue();
			// ["annotation", "p-value", "bonferroni", "gene1", "gene2", ..., "geneN"]
			r[idx++] = row(k, e.getKey(), bonPv.get(k), genes.get(k), desc.get(k));			
		}
		
		return r;
	}
	
	private String[] row(String a, Double v, String bonPv, List<String> genes, String desc) {
//		Log.debug(this.getClass().getName()+"#row arguments "+a+" "+Double.toString(v)+" "+genes);
		String[] r = new String[genes.size() + 4];
		int i = 2;
		r[0] = a; r[1] = Double.toString(v); r[2] = bonPv;
		r[r.length - 1] = desc;
		for (String s : genes) {
			r[++i] = s;
		}
//		Log.debug(this.getClass().getName()+"#row "+Arrays.toString(r));
		return r;
	}
	
	private String selectQuery(String attr, String[] toConvert) {
		Log.debug(this.getClass().getName() + "#issueQuery attribute: "+ attr);
		String value = "", sep = ",", q = null;
		for (int i = 0, len = toConvert.length; i < len; ++i) {
			value += i != len-1 ? toConvert[i] + sep : toConvert[i];
		}
		
		if (attr.equalsIgnoreCase("Gene Ontology (GO)")) {
			q = "<!DOCTYPE Query><Query client=\"true\" processor=\"TSVX\" limit=\"-1\" header=\"1\"><Dataset name=\"go\" config=\"go_config\"><Attribute name=\"go__name_101\"/><Attribute name=\"go__description_101\"/><Attribute name=\"go__domain_101\"/><Filter name=\"go__name_101\" value=\""+value+"\"/></Dataset></Query>";
		} else if (attr.equalsIgnoreCase("OMIMDiseasesHuman") 
				|| attr.equalsIgnoreCase("OMIMDiseases")) {
			q = "<!DOCTYPE Query><Query client=\"true\" processor=\"TSVX\" limit=\"-1\" header=\"1\"><Dataset name=\"bio_mart_fake_db\" config=\"mims_config\"><Attribute name=\"mims__mim_morbid_accession_101\"/><Attribute name=\"mims__mim_morbid_description_101\"/><Filter name=\"mims__mim_morbid_accession_101\" value=\""+value+"\"/></Dataset></Query>";
		}
		
		return q;
	}
	
	private Map<String, String> issueQuery(String q) throws TechnicalException, IOException {
		Log.debug(this.getClass().getName() + "#issueQuery query: "+ q);
		ByteArrayOutputStream o = new ByteArrayOutputStream();
		String oxml = this.pp.getXML(), desc;
		String[] splittedDesc;
		Map<String, String> m = new HashMap<String, String>();
		
		this.pp.setXml(q);
		DefaultPreprocess dp = new DefaultPreprocess(this.pp);
		dp.run(o);
		desc = new String(o.toByteArray(), "UTF-8");
		splittedDesc = desc.trim().split("\n");
		for (int i = 1; i < splittedDesc.length; ++i) {
			String[] p = splittedDesc[i].split("\t");
			// Ann, Desc
			m.put(p[0], p.length < 2 ? "No Description" : p[1]);
		}
		this.pp.setXml(oxml);
		return m;
	}
}

