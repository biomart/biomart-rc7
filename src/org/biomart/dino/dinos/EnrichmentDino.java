package org.biomart.dino.dinos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.biomart.api.Query;
import org.biomart.common.resources.Log;
import org.biomart.dino.annotations.Func;
import org.biomart.dino.command.Command;
import org.biomart.dino.command.CommandRunner;
import org.biomart.dino.command.HypgCommand;
import org.biomart.dino.command.HgmcRunner;
import org.biomart.dino.querybuilder.QueryBuilder;
import org.biomart.queryEngine.QueryElement;

import com.google.inject.Inject;
import com.google.inject.name.Named;

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


/**
 * NOTE This implementation assumes the incoming query has only one attribute!
 * 
 * @author luca
 *
 */
public class EnrichmentDino implements Dino {
	static public final String 
			HSAPIENS_DATASET = "hsapiens_gene_ensembl",
			HSAPIENS_CONFIG	 = "hsapiens_gene_ensembl_config",
			HGNC_ATTR		 = "external_gene_id", //"hgnc_symbol";
			ENS2HGNC_FILTER  = "ensembl_gene_id";
	
	@Func(id = "background") String background;
	@Func(id = "sets") String sets;
	@Func(id = "annotation") String annotation;
	@Func(id = "cutoff") String cutoff;
	
	String client;
	
	HypgCommand cmd;
	HgmcRunner cmdRunner;
	QueryBuilder qbuilder;
	
	@Inject
	public EnrichmentDino(HypgCommand cmd, 
						  HgmcRunner cmdRunner, 
						  @Named("JavaApi") QueryBuilder qbuilder) {
		this.cmd = cmd;
		this.cmdRunner = cmdRunner;
		this.qbuilder = qbuilder;
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
	
	public Command getCommand() {
		return cmd;
	}
	
	public CommandRunner getCommandRunner() {
		return cmdRunner;
	}
	
	public QueryBuilder getQueryBuilder() {
		return this.qbuilder;
	}
		
	String buildCommand() {
		return cmd.build();
	}
	
	void runCommand() { 
		cmdRunner.run(buildCommand()); 
	}
	
	public void toEnsembl(String value, OutputStream o) {

	}
	
	void getQueryResults(Query q, OutputStream o) {
		q.getResults(o);
	}
	
	void getQueryResults(Query.Dataset d, OutputStream o) {
		getQueryResults(d.end(), o);
	}

	void getAnnotations(String attribute, String dataset, String config, OutputStream o) {
//		Query q = initQuery()
//				.addDataset(dataset, config)
//				.addAttribute(attribute).end();
//		getQueryResults(q, o);
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

	public String getCutoff() {
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


















