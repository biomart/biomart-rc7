package org.biomart.dino.dinos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.biomart.api.Portal;
import org.biomart.api.Query;
import org.biomart.api.factory.MartRegistryFactory;
import org.biomart.common.resources.Log;
import org.biomart.dino.CommandRunner;
import org.biomart.dino.Command;

import com.google.inject.Inject;

// Such that it can easily use even compound commands
//Command cmd;
//CommandRunner cmdRunner;
//
//Query initQuery()												V
//void getQueryResults(Query q, OutputStream o);					V
//String buildCommand() { return cmd.build(); }
//void runCommand() { cmdRunner.run(buildCommnad()); }
//Query toEnsembl(String filterValue); OPTIONAL
//Query toHgnc(String filterValue) { 
//	return initQuery()
//		.addFilter(...)
//		.addAttribute(TO_HGNC_ATTR)
//		.end();
//}
//OutputStream mkInMemoryStream();
//
//void getAnnotations(String attribute, OutputStream o);			V
//void printResults(String res, OutputStream o);
//
//void setCommand(Command cmd);
//void setRegistryFactory(MartRegistryFactory reg);				V
//void setCommandRunner(CommandRunner cmdRunner);

public class EnrichmentDino implements Dino {
	static public final String 
			HSAPIENS_DATASET = "hsapiens_gene_ensembl",
			HSAPIENS_CONFIG	 = "hsapiens_gene_ensembl_config",
			HGNC_ATTR		 = "external_gene_id", //"hgnc_symbol";
			ENS2HGNC_FILTER  = "ensembl_gene_id";
	
	String background, sets, annotation, client;
	double cutoff;
	
	MartRegistryFactory regFactory;
	
	Command cmd;
	CommandRunner cmdRunner;
	
	public EnrichmentDino() {}
	
	public EnrichmentDino(
			String background, 
			String sets, 
			double cutoff, 
			String annotation,
			String client) {
		
		Log.debug(this.getClass().getName() + " invoked");
		
		this.background = background;
		this.annotation = annotation;
		this.client = client;
		this.cutoff = cutoff;
		this.sets = sets;
	}

	@Override
	public void run(OutputStream out) {
		Log.debug(this.getClass().getName() + "#run(OutputStream) invoked");
		try {
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Inject
	public void setRegistryFactory(MartRegistryFactory regFactory) {
		this.regFactory = regFactory;
	}
	
	@Inject
	public void setCommand(Command cmd) {
		this.cmd = cmd;
	}
	
	@Inject
	public void setCommandRunner(CommandRunner runner) {
		cmdRunner = runner;
	}
	
	String buildCommand() {
		return cmd.build();
	}
	
	void runCommand() { 
		cmdRunner.run(buildCommand()); 
	}
	
	Query toHgnc(String dataset, String config, String filterValue) { 
		return initQuery()
			.addDataset(dataset, config)
			.addFilter(ENS2HGNC_FILTER, filterValue)
			.addAttribute(HGNC_ATTR)
			.end();
	}

	/**
	 * It just initialize a new Query object with basic parameters.
	 * 
	 * @return a new Query.
	 */
	Query initQuery() {
		Portal portal = new Portal(regFactory);
		return new Query(portal)
			.setClient("false")
			.setHeader(false)
			.setLimit(-1)
			.setProcessor("TSVX");
	}
	
	void getQueryResults(Query q, OutputStream o) {
		q.getResults(o);
	}
	
	void getQueryResults(Query.Dataset d, OutputStream o) {
		getQueryResults(d.end(), o);
	}

	void getAnnotations(String attribute, String dataset, String config, OutputStream o) {
		Query q = initQuery()
				.addDataset(dataset, config)
				.addAttribute(attribute).end();
		getQueryResults(q, o);
	}
	
	void getHSapiensAnnotations(String attribute, OutputStream o) {
		getAnnotations(attribute, HSAPIENS_DATASET, HSAPIENS_CONFIG, o);
	}
	
	ByteArrayOutputStream byteStream() {
		return new ByteArrayOutputStream();
	}

	public String getBackground() {
		return background;
	}

	public String getSets() {
		return sets;
	}

	public String getAnnotation() {
		return annotation;
	}

	public String getClient() {
		return client;
	}

	public double getCutoff() {
		return cutoff;
	}
	
	public String toString() {
		return "EnrichmentDino(background = "+ background + "  " +
				"sets = "+ sets + "  " +
				"annotation = "+ annotation + "  " +
				"cutoff = "+ cutoff + "  " +
				"client = "+ client + ")";	
	}
	
	private String toString(ByteArrayOutputStream o) throws UnsupportedEncodingException {
		return o.toString("UTF-8");
	}

	@Override
	public Dino setQuery(org.biomart.queryEngine.Query query) {
		return this;
	}

	@Override
	public Dino setMimes(String[] mimes) {
		return this;
	}
	
}


















