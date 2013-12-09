package org.biomart.preprocess.enrichment;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.biomart.common.resources.Log;

import cern.colt.Arrays;

public class EnrichmentResultParser {

	BufferedReader brList = null, brPv = null;
	
	String delim = null;
	
	// K = Functional term name, V = [score, gene1, gene2, ... geneN]
	Map<String, List<String>> genes = null;
	// <Score, Annotation Name>
	SortedMap<Double, String> ann = null;
	Map<String, String> bonPv = null;
	
	public EnrichmentResultParser() {
		
	}
	
	// delim can be null.
	public EnrichmentResultParser(FileReader resultList,
			FileReader resultPv, String delim) {
		Log.debug(this.getClass().getName() + " invoked");
		brList = new BufferedReader(resultList);
		brPv = new BufferedReader(resultPv);
		bonPv = new HashMap<String, String>();
		genes = new HashMap<String, List<String>>();
		ann = new TreeMap<Double, String>();
		this.delim = delim == null ? "\t" : delim; 
	}
	
	public String[][] getResults() throws IOException {
		Log.debug(this.getClass().getName() + "#getResults invoked "+ (genes.size() == ann.size()));
		populateAnnWithPValue();
		getGeneList();
		String[][] r = new String[ann.size()][];		
		Set<Map.Entry<Double, String>> set = ann.entrySet();
		Iterator<Map.Entry<Double, String>> it = set.iterator();
		Map.Entry<Double, String> e;
		String k = null;
		int idx = 0;
		
		while(it.hasNext()) {
			e = it.next(); k = e.getValue();
			// ["annotation", "p-value", "bonferroni", "gene1", "gene2", ..., "geneN"]
			r[idx++] = row(k, e.getKey(), bonPv.get(k), genes.get(k));			
		}
		
		return r;
	}
	
	private String[] row(String a, Double v, String bonPv, List<String> genes) {
//		Log.debug(this.getClass().getName()+"#row arguments "+a+" "+Double.toString(v)+" "+genes);
		String[] r = new String[genes.size() + 3];
		int i = 2;
		r[0] = a; r[1] = Double.toString(v); r[2] = bonPv;
		for (String s : genes) {
			r[++i] = s;
		}
//		Log.debug(this.getClass().getName()+"#row "+Arrays.toString(r));
		return r;
	}
	
	private void populateAnnWithPValue() throws IOException {
		Log.debug(this.getClass().getName() + "#populateAnnWithPValue invoked");
		String line = null, a; String[] tokens = null;
		Double v;
		while((line = brPv.readLine()) != null) {
			tokens = line.split("\t");
			a = tokens[1]; v = Double.parseDouble(tokens[8]);
			ann.put(v, a);
			bonPv.put(a, tokens[9]);
		}
		brPv.close();
		brPv = null;
	}
	
	// The stream is closed and delete after reading.
	// NOTE: this method must be called first, or i can change impl.
	// such that it inserts instead of pushing, but that'd be a waste.
//	private void populateAnnWithPValue() throws IOException {
//		Log.debug(this.getClass().getName() + "#populateAnnWithPValue invoked");
//		String line = null, a, v; String[] tokens = null;
//		while((line = brPv.readLine()) != null) {
//			tokens = line.split("\t");
//			a = tokens[1]; v = tokens[8];
//			if (!genes.containsKey(a)) {
//				genes.put(a, new ArrayList<String>());
//				// So we can easily return an array with all the data
//				genes.get(a).add(a);
//			}
//			genes.get(a).add(v);
//		}
//		brPv.close();
//		brPv = null;
//	}
	
	// The stream is closed and delete after reading.
	private void getGeneList() throws IOException {
		Log.debug(this.getClass().getName() + "#getGeneList invoked");
		String line = null, a; String[] tokens = null;
		while((line = brList.readLine()) != null) {
			tokens = line.split("\t");
			// Annotation
			a = tokens[1];
			if (!genes.containsKey(a)) {
				genes.put(a, new ArrayList<String>());
			}
			// Add a new gene
			genes.get(a).add(tokens[2]);
		}
		brList.close();
		brList = null;
	}
	
	
}
