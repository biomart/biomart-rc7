package org.biomart.dino;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.biomart.common.resources.Log;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class AttributesConfig {

	public static final String sep = System.getProperty("file.separator");
	public static final String ATTR = "attributes";
	private String confDirPath;
	
	@Inject
	public AttributesConfig(@Named("Dino Properties Directory") String confDirPath) {
		this.confDirPath = confDirPath;
	}
	
	public String getDirPath() {
		return confDirPath;
	}
	
	/**
	 * Keys of the map are name of columns of data whereas values are BioMart's 
	 * attribute names.
	 * 
	 * @param fileName Just the file name of the file inside the configuration folder.
	 * @return Returns a map of (attribute display name, attribute internal name) or an empty map if there's been any error while reading.
	 */
	public Map<String, String> read(String fileName) {
		Properties p = new Properties();
		String path = confDirPath + sep + fileName;
		
		try {
			p.load(new FileInputStream(new File(path)));
		} catch (IOException e) {
			Log.error(this.getClass().getName() + "#read("+ path + ") ", e);
		}
		
		return getData(p);
	}
	
	public Map<String, String> getData(Properties pp) {
		String a = pp.getProperty(ATTR);
		return a == null || a.isEmpty() ? new HashMap<String, String>() : mkMap(a);
	}
	
	/**
	 * Given a string of the form "k0=v0, k1 = v1, ...", it returns a map.
	 * @param attributes
	 * @return
	 */
	public Map<String, String> mkMap(String attributes) {
		String[] entries = attributes.split(",");
		
		for (int i = 0; i < entries.length; ++i) {
			entries[i] = entries[i].trim();
		}
		
		Map<String, String> m = new HashMap<String, String>(entries.length);
		
		for (String s : entries) {
			String[] kv = s.split("=");
			m.put(kv[0].trim(), kv[1].trim());
		}
		
		return m;
	}
	
	
}
