package org.biomart.dino.querybuilder;

import java.io.OutputStream;

public interface QueryBuilder {

	public QueryBuilder initQuery(boolean header, String client, int limit, String proc);
	
	public QueryBuilder getResults(OutputStream out);

	public boolean hasHeader();

	public QueryBuilder setHeader(boolean header);

	public String getClient();

	public QueryBuilder setClient(String client);

	public String getProcessor();

	public QueryBuilder setProcessor(String proc);

	public int getLimit();

	public QueryBuilder setLimit(int limit);
	
	public QueryBuilder addFilter(String name, String value);
	
	public QueryBuilder addAttribute(String name);
	
	public String getXml();
	
	public QueryBuilder setDataset(String name, String config);
	
	public String getDatasetName();
	
	public String getDatasetConfig();
	
}
