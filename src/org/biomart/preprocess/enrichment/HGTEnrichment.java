package org.biomart.preprocess.enrichment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import org.biomart.common.exceptions.TechnicalException;
import org.biomart.common.resources.Log;
import org.biomart.preprocess.EnsembleTranslation;
import org.biomart.preprocess.PreprocessParameters;
import org.biomart.preprocess.utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class HGTEnrichment extends Enrichment {
	static final String BACKGROUND_FILTER = "background";
	static final String SETS_FILTER = "sets";
	
	Configuration cfg;
	
	String playground, runner, annotation, cutoff = "0.003";
	
	File setsFile = null, backgroundFile = null;
	
	public HGTEnrichment(PreprocessParameters params) {
		super(params);
		
		try {
			cfg = new Configuration();
			cfg.load();
		} catch (IOException e) {
			Log.error("HGTEnrichment::HGTEnrichment ", e);
		}
		
		getProperties();
		
	}
	
	private void getProperties() {
		playground = cfg.getProperty("enrichment.dir");
		runner = cfg.getProperty("enrichment.runner");
		annotation = cfg.getProperty("enrichment.annotation_file");
	}

	@Override
	public String getContentType() {
		return "plain/text";
	}


	@Override
	public void runEnrichment() {
		Log.debug(this.getClass().getName() + "#runEnrichment invoked");
		
		Document d = Utils.parseXML(params.getXML());
		
		FileOutputStream setsStream = null, bkStream = null;
		
		try {
			File workPath = new File(playground);
			setsFile = File.createTempFile("sets", "", workPath);
			backgroundFile = File.createTempFile("background", "", workPath);
			setsStream  = new FileOutputStream(setsFile);
			bkStream = new FileOutputStream(backgroundFile);
		
			Log.debug(this.getClass().getName() + " translating into Ensembles IDs...");
			makeSets(d, setsStream);
			makeBackground(d, bkStream);
						
			runProcess();
			//getResults();
		} catch (Exception e) {
			Log.error("HGTEnrichment#runEnrichment ", e);
		} finally {
			try {
				setsStream.close();
				bkStream.close();
			} catch (IOException e) {
				// Seriously?
				Log.error("HGTEnrichment#runEnrichment ", e);
			}
		}
	}
	
	private void makeSets(Document doc, OutputStream o) throws TechnicalException, IOException {
		o.write(">fracchia\n".getBytes());
		Document d = removeAllButThisFilter(doc, "sets");
		Log.debug(this.getClass().getName() + " starting translation for sets");
		new EnsembleTranslation(this.params, d).run(o);
		try {
			o.write("<fracchia\n".getBytes());
		} catch (IOException e) {
			FileOutputStream fo = new FileOutputStream(setsFile);
			fo.write("<fracchia\n".getBytes());
			fo.close();
		}
	}
	
	private void makeBackground(Document doc, OutputStream o) throws TechnicalException, IOException {
		Document d = removeAllButThisFilter(doc, "background");
		Log.debug(this.getClass().getName() + " starting translation for background");
		new EnsembleTranslation(this.params, d).run(o);	
	}
	
	private Document removeAllButThisFilter(Document doc, String filter) {
		// Remove other filter
		Document d = Utils.copy(doc);
		Element dataset = (Element)d.getElementsByTagName("Dataset")
				.item(0), e, aFilter = null;
		
		Node[] filters = Utils.removeElement(d, "Filter");
		
		for (Node n : filters) {
			e = (Element) n;
			if (e.getAttribute("name").equalsIgnoreCase(filter)) {
				e.setAttribute("name", "hgnc_symbol");
				aFilter = e;
				break;
			}
		}
		if (aFilter == null) {
			Log.error(this.getClass().getName() + " `sets` filter is nowhere to be found!");
			return null;
		}	
		dataset.appendChild(aFilter);
		return d;
	}
	
	private String buildCommand() {
		return runner 
				+ " -g "+ backgroundFile.getAbsolutePath() 
				+ " -a "+ annotation
				+ " -c "+ cutoff
				+ " -s "+ setsFile.getAbsolutePath();
	}
	
	public void runProcess() {
		Runtime rt = Runtime.getRuntime();
		Process pr = null;
		String cmd = buildCommand();
		int counter = 0, maxWait = 222222360, result = -42;
		try {
			Log.debug("HGTEnrichment#runProcess executing command "+cmd);
			Log.debug("HGTEnrichment#runProcess inside dir "+ playground);
			pr = rt.exec(cmd, null, new File(playground));
			String className = this.getClass().getName();
			while(true) {
				Log.debug(className + " result = "+ result + " (default value), the process didn't finish yet...");
				Thread.sleep(500);
				try {
					result = pr.exitValue();
					break;
				} catch(IllegalThreadStateException e) {
					if (++counter < maxWait) continue;
					break;
				}
			}
			if (result != 0)
				throw new IOException("Process didn't terminate or retured a value of error: "+ result);
		} catch (IOException e) {
			Log.error("HGTEnrichment#runProcess error during runner execution", e);
		} catch (InterruptedException e) {
			Log.error("HGTEnrichment#runProcess runner has been interrupted", e);
		} finally {
			//if (setsFile != null) setsFile.delete();
			//if (backgroundFile != null) backgroundFile.delete();
		}
	}
	
	private void printInputs(String sets, String bk) throws IOException {
		File workPath = null;
		PrintWriter spw = null, bpw = null;
		try {
			workPath = new File(playground);
			setsFile = File.createTempFile("sets", "", workPath);
			backgroundFile = File.createTempFile("background", "", workPath);
			spw = new PrintWriter(setsFile);
			bpw = new PrintWriter(backgroundFile);
			spw.print(sets);
			bpw.print(bk);
		} finally {
			if (spw != null) spw.close();
			if (bpw != null) bpw.close();
		}
	}
	
	public String makeBackground(String bk) {
		String[] s = bk.split(",");
		StringBuilder b = new StringBuilder(s.length);
		for (String e : s) 
			b.append(e + "\n");
		return b.toString();
	}
	
	public String makeSet(String setBegin, String genes, String setEnd) {
		String[] gs = genes.split(",");
		StringBuilder b = new StringBuilder(gs.length + 2);
		String d = "\n";
		
		b.append(setBegin + d);
		for (String s : gs) {
			b.append(s + d);
		}
		b.append(setEnd + d);
		
		return b.toString();
	}
	
	

}
