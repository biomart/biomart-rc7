package org.biomart.preprocess;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.biomart.common.exceptions.ValidationException;
import org.biomart.common.resources.Log;
import org.biomart.preprocess.factory.PreprocessFactory;

public class PreprocessController {

	public static Preprocess newPreprocess(String name, PreprocessParameters pp) {
		// Returns a PreprocessFactory class
		Class<? extends PreprocessFactory> factoryKlass = PreprocessRegistry.get(name);
		Preprocess p = null;
		
		if (factoryKlass == null) return null;
		
		try {
			p = getPreprocessInstance(factoryKlass, pp);
		} catch (SecurityException e) {
			
		} catch (IllegalArgumentException e) {
			Log.error("PreprocessController#newPreprocess: ", e);
		} catch (NoSuchMethodException e) {
			Log.error("PreprocessController#newPreprocess: ", e);
		} catch (InstantiationException e) {
			Log.error("PreprocessController#newPreprocess: ", e);
		} catch (IllegalAccessException e) {
			Log.error("PreprocessController#newPreprocess: ", e);
		} catch (InvocationTargetException e) {
			Log.error("PreprocessController#newPreprocess: ", e);
		}
		
		return p;
	}
	
    private static Preprocess getPreprocessInstance( // Too many exception, Java!
    		Class<? extends PreprocessFactory> fc, PreprocessParameters pp) 
    				throws SecurityException, NoSuchMethodException, 
    				IllegalArgumentException, InstantiationException, 
    				IllegalAccessException, InvocationTargetException {
    		
    		Constructor<? extends PreprocessFactory> ctor = 
    				fc.getDeclaredConstructor(new Class<?>[]{});
    		
    		PreprocessFactory pf = ctor.newInstance();
    	
		return pf.newPreprocess(pp);
		
    }
     
}
