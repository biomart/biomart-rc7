package org.biomart.preprocess.enrichment;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import org.biomart.common.resources.Log;
import org.biomart.preprocess.PreprocessParameters;
import org.w3c.dom.Document;

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
		
		playground = cfg.getProperty("enrichment.dir");
		runner = cfg.getProperty("enrichment.runner");
		annotation = cfg.getProperty("enrichment.annotation_file");
		Log.debug(cfg);
	}

	@Override
	public String getContentType() {
		return "plain/text";
	}


	@Override
	public void runEnrichment() {
		String setBegin = ">fracchia", setEnd ="<fracchia", sets, bk;
		
		Document d = parseXML(params.getXML());
		
		try {
			sets = getFilterContent(d, SETS_FILTER);
			printInputs(
					makeSet(setBegin, sets, setEnd),
					makeBackground(getFilterContent(d, BACKGROUND_FILTER))
			);
			runProcess();
			//getResults();
		} catch (Exception e) {
			Log.error("HGTEnrichment#runEnrichment ", e);
		}
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
		int counter = 0, maxWait = 60, result = -42;
		try {
			Log.debug("HGTEnrichment#runProcess executing command "+cmd);
			Log.debug("HGTEnrichment#runProcess inside dir "+ playground);
			pr = rt.exec(cmd, null, new File(playground));
			while(true) {
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
			if (setsFile != null) setsFile.delete();
			if (backgroundFile != null) backgroundFile.delete();
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
