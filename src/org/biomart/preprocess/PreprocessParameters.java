package org.biomart.preprocess;

import org.biomart.objects.objects.MartRegistry;

public class PreprocessParameters {

	private String xml, user;
	private String[] mimes;
	private MartRegistry registry;
	private boolean isCountQuery;
	
	public PreprocessParameters(String xml, MartRegistry registry,
			String user, String[] mimes, boolean isCountQuery) {
		this.xml = xml;
		this.user = user;
		this.mimes = mimes;
		this.registry = registry;
		this.isCountQuery = isCountQuery;
	}

	public String getXML() {
		return xml;
	}
	public void setXml(String xml) {
		this.xml = xml;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String[] getMimes() {
		return mimes;
	}
	public void setMime(String[] mimes) {
		this.mimes = mimes;
	}
	public boolean getCountQuery() {
		return isCountQuery;
	}
	public void setCountQuery(boolean isCountQuery) {
		this.isCountQuery = isCountQuery;
	}
	public MartRegistry getRegistry() {
		return registry;
	}
	
	
}
