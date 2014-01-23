package org.biomart.dino.dinos;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.biomart.common.resources.Log;
import org.biomart.dino.MetaData;
import org.biomart.dino.Utils;
import org.biomart.dino.annotations.Func;
import org.biomart.dino.command.Command;
import org.biomart.dino.command.CommandRunner;
import org.biomart.dino.command.HypgCommand;
import org.biomart.dino.command.HgmcRunner;
import org.biomart.dino.querybuilder.QueryBuilder;
import org.biomart.objects.objects.Attribute;
import org.biomart.objects.objects.Element;
import org.biomart.queryEngine.Query;
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
			BACKGROUND = "background",
			SETS = "sets",
			ANNOTATION = "annotation",
			CUTOFF = "cutoff";
	
	@Func(id = BACKGROUND) String background;
	@Func(id = SETS) String sets;
	@Func(id = ANNOTATION) String annotation;
	@Func(id = CUTOFF) String cutoff;
	
	String client;
	Query q;
	
	HypgCommand cmd;
	HgmcRunner cmdRunner;
	QueryBuilder qbuilder;
	MetaData metadata;
	
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
	
	/**
	 * Submits a query for gene id translation.
	 * 
	 * @param function The function of the filter of which value must be translated.
	 * @param attributeList Attribute list that includes info mandatory for translation. 
	 * @param filterName 
	 * @param filterValue Value to translate.
	 * @param o
	 */
	public void toEnsemblGeneId(String attributeList, 
								String filterName, 
								String filterValue, 
								OutputStream o) {
		Element tmpe = null;
		Attribute forTransAttr = null;
		QueryElement qelem = null;
		// We can get the queryelement from the function of element it wraps.
		Map<String, QueryElement> bindings = this.metadata.getBindings();
		
		qelem = bindings.get(attributeList);
		
		tmpe = Utils.getAttributeForEnsemblGeneIdTranslation(qelem);
		
		// It means it didn't find a filter list or qelem doesn't wrap an
		// attribute list.
		if (tmpe == null) {
			Log.error(this.getClass().getName() + "#toEnsemblGeneId(): "+
					"cannot get the necesary attribute needed for translation. "+
					"Maybe "+ attributeList + " is not an attribute list?");
			return;
		}
		
		
		
		forTransAttr = (Attribute) tmpe;
		
		submitToEnsemblIdQuery(forTransAttr, filterName, filterValue, o);
	}
	
	
	private void submitToEnsemblIdQuery(Attribute attr,
										    String filterName,
										    String filterValue,
										    OutputStream o) {
		initQueryBuilder();
		qbuilder.setDataset(Utils.getDatasetName(attr), 
							Utils.getDatasetConfig(attr))
				.addAttribute(attr.getName())
				.addFilter(filterName, filterValue)
				.getResults(o);
	}
	
	
	/**
	 * Retrieves annotations based on the input attribute from the query.
	 * @param attributeList
	 * @param o
	 */
	public void queryForAnnotations(String attributeList, OutputStream o) {
		Element tmpe = null;
		Attribute attr = null;
		QueryElement qelem = null;
		// We can get the queryelement from the function of element it wraps.
		Map<String, QueryElement> bindings = this.metadata.getBindings();
		
		qelem = bindings.get(attributeList);
		
		tmpe = Utils.getAttributeForEnsemblSpecieIdTranslation(qelem);
		
		// It means it didn't find a filter list or qelem doesn't wrap an
		// attribute list.
		if (tmpe == null) {
		Log.error(this.getClass().getName() + "#toEnsemblGeneId(): "+
			"cannot get the necesary attribute needed for translation. "+
			"Maybe "+ attributeList + " is not an attribute list?");
			return;
		}
		
		
		
		attr = (Attribute) tmpe;
		
		submitAnnotationsQuery(attr, o);
	}
	
	
	private void submitAnnotationsQuery(Attribute attr, OutputStream o) {
		initQueryBuilder();
		qbuilder.setDataset(Utils.getDatasetName(attr), Utils.getDatasetConfig(attr))
			.addAttribute(attr.getName())
			.getResults(o);
	}
	
	
	private void initQueryBuilder() {
		qbuilder.init();
	}
	
	
	public ByteArrayOutputStream byteStream() {
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
		q = query;
		return this;
	}

	@Override
	public Dino setMimes(String[] mimes) {
		return this;
	}
	
	@Override
	public Dino setMetaData(MetaData md) {
		this.metadata = md;
		return this;
	}
	
	public MetaData getMetaData() {
		return this.metadata;
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
	
}


















