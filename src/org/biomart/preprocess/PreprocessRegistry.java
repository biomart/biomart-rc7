package org.biomart.preprocess;

import java.util.HashMap;
import java.util.Map;

import org.biomart.common.resources.Log;

public class PreprocessRegistry {
    private static Map<String,Class> lookup = new HashMap<String,Class>();

	static boolean register(String process, Class klass) {
		boolean r;
		
		if (r = Preprocess.class.isAssignableFrom(klass)) {
            lookup.put(normalizeName(process), klass);
            Log.info("Registered processor " + process);
        } else {
            Log.error("Registered classes must be Process type");
        }
		
		return r;
	}
	
    private static boolean isInstalled = false;

	public static void install() {
		if (isInstalled) {
			return;
		}
		
		isInstalled = true;
		
		// Registration
		register("Default", DefaultPreprocess.class);
	}
	
	public static Class get(String process) {
        return lookup.get(normalizeName(process));
	}
	
	static String normalizeName(String process) {
		return process.toUpperCase();
	}
	
}
