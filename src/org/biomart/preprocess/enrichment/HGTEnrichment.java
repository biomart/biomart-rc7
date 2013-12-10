package org.biomart.preprocess.enrichment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

import org.biomart.common.exceptions.TechnicalException;
import org.biomart.common.resources.Log;
import org.biomart.preprocess.EnsembleTranslation;
import org.biomart.preprocess.PreprocessParameters;
import org.biomart.preprocess.utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Joiner;

public class HGTEnrichment extends Enrichment {
	static final String BACKGROUND_FILTER = "background_list";
	static final String SETS_FILTER = "sets_list";
	static final String FILTER_FUNC_ATTR = "filter_list";
//	static final String ATTR_NAME = "name";
	static final String ATTR_NAME = "value"; // "hgnc_symbol";
	static final String CUTOFF_FILTER = "transcript_count_copy";

	Configuration cfg;

	String playground, runner, annotation, cutoff;

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
	public void runEnrichment(OutputStream o) {
		Log.debug(this.getClass().getName() + "#runEnrichment invoked");

		Document d = Utils.parseXML(params.getXML());

		FileOutputStream setsStream = null, bkStream = null;

		try {
			File workPath = new File(playground);
			setsFile = File.createTempFile(SETS_FILTER, "", workPath);
			backgroundFile = File.createTempFile(BACKGROUND_FILTER, "", workPath);
			setsStream  = new FileOutputStream(setsFile);
			bkStream = new FileOutputStream(backgroundFile);
			
			cutoff = getCutOff(d);

			Log.debug(this.getClass().getName() + " translating into Ensembles IDs...");
			makeSets(d, setsStream);
			makeBackground(d, bkStream);
//			selectAnnotationFile(d);

			runProcess();
			printResults(o, getResults());
		} catch (Exception e) {
			Log.error("HGTEnrichment#runEnrichment ", e);
		} finally {
			try {
				o.close();
				setsStream.close();
				bkStream.close();
			} catch (IOException e) {
				// Seriously?
				Log.error("HGTEnrichment#runEnrichment ", e);
			}
		}
	}
	
	private void selectAnnotationFile(Document doc) {
		Element attr = null;
		String name = null, match = "external_gene_id";
		NodeList nl = doc.getDocumentElement().getElementsByTagName("Attribute");
		if (nl.getLength() > 0) {
			for (int i = 0, len = nl.getLength(); i < len; ++i) {
				attr = (Element) nl.item(0);
				name = attr.getAttribute("name");
				if (match.equalsIgnoreCase(name)) {
					annotation = cfg.getProperty("diseases_file");
					return;
				}
					
			}
		}
	}

	private String[][] getResults() {
		Log.debug(this.getClass().getName() + "#getResults invoked");
		String d = System.getProperty("file.separator");
		String[][] r = null;
		try {
			EnrichmentResultParser e = new EnrichmentResultParser(
					new FileReader(playground + d + "hypg.list"),
					new FileReader(playground + d + "hypg.pv"),
					null
			);
			r = e.getResults();
		} catch (FileNotFoundException e) {
			Log.error(this.getClass().getName() + "#getResults cannot find/open the result files ", e);
		} catch (IOException e) {
			Log.error(this.getClass().getName() + "#getResults cannot find/open the result files ", e);
		}
		return r;
	}

	// NOTE that if r is null, it prints the header only.
	private void printResults(OutputStream o, String[][] r) throws IOException {
		Log.debug(this.getClass().getName() + "#printResults invoked");
		String d = "\t", lr = "\n", genes;
		String[] line = null;
		// Write the header first
		o.write(("Annotation"+d+"P-value"+d+"Bonferroni P-value"+d+"Genes"+lr).getBytes());
		for (int i = 0; i < r.length && i < 5; ++i) {
			line = r[i];
			genes = Joiner.on(",").join(Arrays.copyOfRange(line, 3, line.length));
			o.write((line[0]+d+line[1]+d+line[2]+d+genes+lr).getBytes());
		}
		o.flush();
	}

	private void makeSets(Document doc, OutputStream o) throws TechnicalException, IOException {
		// Temporary hack
		String s = null;
		Element filter = null;
		Document d = removeAllButThisFilterList(doc, SETS_FILTER);
		if (d == null || d.getDocumentElement()
				.getElementsByTagName("Filter").getLength() == 0) {
			Node[] filters = Utils.removeElement(d = Utils.copy(doc), "Filter");
			Element dataset = (Element)d.getElementsByTagName("Dataset")
					.item(0);
			for (Node n : filters) {
				filter = (Element) n;
				s = filter.getAttribute(FILTER_FUNC_ATTR);
				if (!(s.equalsIgnoreCase(BACKGROUND_FILTER) 
					|| s.equalsIgnoreCase(CUTOFF_FILTER))) {
					dataset.appendChild(filter);
				}
			}
		}
		// End temporary hack
		
		
		o.write(">fracchia\n".getBytes());
//		Log.debug(this.getClass().getName() + "#makeSets query "+ Utils.toXML(d));
//		Element f = (Element)d.getDocumentElement().getElementsByTagName("Filter").item(0);
//		String data = f.getAttribute(ATTR_NAME);
//		Log.debug(this.getClass().getName() + "#makeSets data "+ data);
//		o.write(makeSet(">fracchia", data, "<fracchia").getBytes());
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
//		Log.debug(this.getClass().getName() + "#makeBackground query "+ Utils.toXML(doc));
		Document d = removeAllButThisFilterList(doc, BACKGROUND_FILTER);
//		Element f = (Element)d.getDocumentElement().getElementsByTagName("Filter").item(0);
//		String data = f.getAttribute(ATTR_NAME);
//		o.write(makeBackground(data).getBytes());
		Log.debug(this.getClass().getName() + " starting translation for background");
		new EnsembleTranslation(this.params, d).run(o);
	}
	
	private String getCutOff(Document doc) {
		NodeList nl = doc.getDocumentElement().getElementsByTagName("Filter");
		String v = null;
		Element e = null;
		for (int i = 0, len = nl.getLength(); i < len; ++i) {
			e = (Element) nl.item(i);
			if (e.getAttribute("name").equalsIgnoreCase(CUTOFF_FILTER)) {
				return e.getAttribute(ATTR_NAME); 
			}
		}
		return "";
	}

	private Document removeAllButThisFilterList(Document doc, String filter) {
		// Remove other filter
		Document d = Utils.copy(doc);
		Element dataset = (Element)d.getElementsByTagName("Dataset")
				.item(0), e, aFilter = null;

		Node[] filters = Utils.removeElement(d, "Filter");
		Log.debug(this.getClass().getName() + "#removeAllButeThisFilter d "+ Utils.toXML(d));

		for (Node n : filters) {
			e = (Element) n;
			if (e.getAttribute(FILTER_FUNC_ATTR).equalsIgnoreCase(filter)) {
				aFilter = e;
				break;
			}
		}
		if (aFilter == null) {
			Log.error(this.getClass().getName() + " "+ filter +" filter is nowhere to be found!");
			return null;
		}
		dataset.appendChild(aFilter);
		return d;
	}
	
	private Document removeAllButThisFilter(Document doc, String filterList, String filter) {
		Document d = Utils.copy(doc);
		Element dataset = (Element)d.getElementsByTagName("Dataset")
				.item(0), e, aFilter = null;

		Node[] filters = Utils.removeElement(d, "Filter");

		for (Node n : filters) {
			e = (Element) n;
			if (e.getAttribute(FILTER_FUNC_ATTR).equalsIgnoreCase(filterList)
				&& e.getAttribute("name").equalsIgnoreCase(filter)) {
				aFilter = e;
				break;
			}
		}
		if (aFilter == null) {
			Log.error(this.getClass().getName() + " "+ filter +" filter is nowhere to be found!");
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
//				+ " -B";
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
