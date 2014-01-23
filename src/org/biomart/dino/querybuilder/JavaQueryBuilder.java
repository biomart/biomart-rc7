package org.biomart.dino.querybuilder;

import java.io.OutputStream;

import org.biomart.api.Query;

import com.google.inject.Inject;

public class JavaQueryBuilder implements QueryBuilder {
	
	private Query q;
	private boolean header;
	private String client;
	private String proc;
	private int limit;
	private Query.Dataset dataset;
	private String datasetName, datasetConfig;
	
	/**
	 * 
	 * By default, it starts with a query with this parameters:
	 * header = false
	 * client = "false"
	 * processor = "TSVX"
	 * limit = -1, that is not limit
	 * empty dataset name and configuration
	 * 
	 * @param q - A new prestine Query object.
	 */
	@Inject
	public JavaQueryBuilder(Query q) {
		this.q = q;
		this.header = false;
		this.client = "false";
		this.proc = "TSVX";
		this.limit = -1;
		this.dataset = null;
		this.datasetName = "";
		this.datasetConfig = "";
	}

	@Override
	public QueryBuilder getResults(OutputStream out) {
		q.getResults(out);
		
		return this;
	}

	@Override
	public boolean hasHeader() {
		return header;
	}

	@Override
	public QueryBuilder setHeader(boolean header) {
		q.setHeader(this.header = header);
		return this;
	}

	@Override
	public String getClient() {
		return this.client;
	}

	@Override
	public QueryBuilder setClient(String client) {
		q.setClient(this.client = client);
		return this;
	}

	@Override
	public String getProcessor() {
		return this.proc;
	}

	@Override
	public QueryBuilder setProcessor(String proc) {
		q.setProcessor(this.proc = proc);
		return this;
	}

	@Override
	public int getLimit() {
		return this.limit;
	}

	@Override
	public QueryBuilder setLimit(int limit) {
		q.setLimit(this.limit = limit);
		return this;
	}

	@Override
	public QueryBuilder addFilter(String name, String value) {
		this.dataset.addFilter(name, value);
		return this;
	}

	@Override
	public QueryBuilder addAttribute(String name) {
		this.dataset.addAttribute(name);
		return this;
	}

	@Override
	public String getXml() {
		return q.getXml();
	}

	@Override
	public QueryBuilder setDataset(String name, String config) {
		dataset = q.addDataset(this.datasetName = name,
								this.datasetConfig = config);
		return this;
	}

	@Override
	public String getDatasetName() {
		return datasetName;
	}
	
	@Override
	public String getDatasetConfig() {
		return datasetConfig;
	}

}
