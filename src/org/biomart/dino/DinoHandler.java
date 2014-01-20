package org.biomart.dino;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.biomart.common.exceptions.ValidationException;
import org.biomart.common.resources.Log;
import org.biomart.dino.annotations.Func;
import org.biomart.dino.dinos.Dino;
import org.biomart.objects.objects.Attribute;
import org.biomart.objects.objects.Element;
import org.biomart.queryEngine.Query;
import org.biomart.queryEngine.QueryElement;
import org.biomart.common.utils.XMLElements;

/**
 * 
 * @author Luca Pandini (lpand)
 * 
 * gets hold of the Dino controller class given the dino field; it creates
 * a new instance of the controller and hands it off to the Binder.
 * 
 * The Binder will gather the Dino controller requirements and provide
 * them to it. Eventually, it'll invoke the controller's create method.
 *
 */
public class DinoHandler {

	private DinoHandler() {}
	
	public static void 
	runDino(Query q, String user, String[] mimes, OutputStream o) throws IOException {
		Log.debug("DinoHandler#runDino() invoked");

		Class<? extends Dino> dinoClass;
		Dino dino;
		String dinoName = q.getDino();
				
		try {
			
			dinoClass = getDinoClass(dinoName);
			List<Field> fields = getAnnotatedFields(dinoClass);
			dino = getDinoInstance(dinoClass);
			setFieldValues(dino, fields, q.getQueryElementList());
			dino.setQuery(q)
				.setMimes(mimes)
				.run(o);
			
		} catch (ClassNotFoundException e) {
			Log.error("DinoHandler#runDino Class<"+ dinoName +"> not found.", e);
			o.close();
		} catch (IllegalArgumentException |
				 InstantiationException   |
				 IllegalAccessException   |
				 InvocationTargetException e) {
			Log.error("DinoHandler#runDino ", e);
			o.close();
		}
	}
	
	public static Class<? extends Dino> 
	getDinoClass(String dinoClassName) throws ClassNotFoundException {
		Log.debug("DinoHandler#getDinoClass() invoked");
		
		Class<? extends Dino> dinoClass;
		try {
			dinoClass = Class.forName(dinoClassName).asSubclass(Dino.class);
		} catch (RuntimeException re) {
			Log.error("DinoHandler#getDinoClass() Dino name `"+ dinoClassName 
					+ "` doesn't correspond to any class");

			// The stream should be closed by the caller.
			throw re;
		}
		
		return dinoClass;
	}
	
	public static Dino
	getDinoInstance(Class<? extends Dino> klass) throws IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		Log.debug("DinoHandler#getDinoInstance() invoked");
		
		Constructor<?>[] ctors = klass.getConstructors();
		Constructor<?> ctor = null;
		for (int i = 0; i < ctors.length; ++i) {
			ctor = ctors[i];
			if (ctor.getGenericParameterTypes().length == 0) {
				break;
			}
		}
		
		ctor.setAccessible(true);
		Dino dino = Dino.class.cast(ctor.newInstance());
		
		return dino;
	}
	
	/**
	 * Explores klass' fields and returns the ones annotated with the Func
	 * annotation.
	 * 
	 * @param klass
	 * @return a list of fields annotated with the Func annotation.
	 */
	public static List<Field> 
	getAnnotatedFields(Class<?> klass) {
		// All fields, included the interfaces' ones
		Field[] fds = klass.getDeclaredFields();
		List<Field> fields = new ArrayList<Field>(fds.length);
		
		for (Field f : fds) {
			if (f.isAnnotationPresent(Func.class) 
				&& f.getType() == String.class) {
				
				fields.add(f);
			
			}
		}
		
		return fields;
	}
	
	/**
	 * 
	 * Note that it modifies the original fields argument.
	 * 
	 * @param b The builder instance on which set field values.
	 * @param fields builder's class' fields annotated with the Func annotation.
	 * @param qes elements coming from the query.
	 * 
	 * @throws IllegalArgumentException
	 * @throws IllegalAccessException
	 * @throws ValidationException if there's any mandatory function parameter missing.
	 */
	public static void
	setFieldValues(Dino b, List<Field> fields, List<QueryElement> qes) throws IllegalArgumentException, IllegalAccessException {
		XMLElements key = XMLElements.FUNCTION;
		String propVal = null;
		Element e = null;
		Func a = null;
		ArrayList<Field> fieldsCp = null;
		
		for (QueryElement q : qes) {
			e = q.getElement();
			// Get its function name
			propVal = e.getPropertyValue(key);
			
			fieldsCp = new ArrayList<Field>(fields);
			for (Field f : fieldsCp) {
				a = f.getAnnotation(Func.class);
				if (a.id().equalsIgnoreCase(propVal)) {
					f.setAccessible(true);
					f.set(b, getElementValue(q));
					f.setAccessible(false);
					fields.remove(f);
				}
			}
			
			if (fields.size() == 0) return;
		}
		
		for (Field f : fields) {
			if (! f.getAnnotation(Func.class).optional()) {
				throw new ValidationException("Function parameter `"
						+ f.getAnnotation(Func.class).id() 
						+ "` missing");
			}
		}
	}
	
	
	public static String getElementValue(QueryElement qe) {
		String value = "";
		
		switch(qe.getType()) {
			case ATTRIBUTE: 
				Attribute a = (Attribute) qe.getElement();
				value = a.getName();
				break;
			case FILTER:
				value = qe.getFilterValues();
				break;
			default:
				break;
		}
		
		return value;
	}
}

























































