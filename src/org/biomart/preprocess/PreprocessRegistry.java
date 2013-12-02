package org.biomart.preprocess;

import java.util.HashMap;
import java.util.Map;

import org.biomart.common.resources.Log;
import org.biomart.preprocess.factory.DefaultPreprocessFactory;
import org.biomart.preprocess.factory.HGTEnrichmentPreprocessFactory;
import org.biomart.preprocess.factory.NetworkPreprocessFactory;
import org.biomart.preprocess.factory.PreprocessFactory;

public class PreprocessRegistry {
    private static Map<String,Class<? extends PreprocessFactory>> lookup 
    		= new HashMap<String,Class<? extends PreprocessFactory>>();

	static void register(String process, Class<? extends PreprocessFactory> klass) {
        lookup.put(normalizeName(process), klass);
        Log.info("Registered processor " + process);
	}
	
    private static boolean isInstalled = false;

	public static void install() {
		if (isInstalled) {
			return;
		}
		
		isInstalled = true;
		
		// Registration
		register("DefaultPreprocess", DefaultPreprocessFactory.class);
		register("Network", NetworkPreprocessFactory.class);
		register("Enrichment", HGTEnrichmentPreprocessFactory.class);
	}
	
	public static Class<? extends PreprocessFactory> get(String process) {
        return lookup.get(normalizeName(process));
	}
	
	static String normalizeName(String process) {
		return process.toUpperCase();
	}
	
}
