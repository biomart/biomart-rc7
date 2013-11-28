package org.biomart.preprocess.enrichment;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class Configuration {
	public static final String CONF_FILE = "enrichment.properties";
	public static final String DELIM = System.getProperty("file.separator");
	Properties configFile;
	String configFilePath;
	
	public Configuration() throws IOException {
		this(System.getProperty("user.dir")+ DELIM + "preprocesses"+DELIM+"enrichment");
	}
	
	public  Configuration(String basedir) {
		//String basedir = System.getProperty("biomart.basedir", "."),
		configFilePath = basedir + DELIM + CONF_FILE;
		configFile = new Properties();
	}
	
	public void load() throws IOException {
		ClassLoader ld = this.getClass().getClassLoader();
		
		try {
			configFile.load(new FileInputStream(new File(configFilePath)));
		} catch (IOException e) {
			// Log.error("Configuration#loadConfig cannot find configuration file "+configFilePath);
			e.printStackTrace();
			throw e;
		}
	}
	
	public String getProperty(String key) {
		return this.configFile.getProperty(key);
	}
	
	public String getConfigPath() {
		return configFilePath;
	}
	

}
