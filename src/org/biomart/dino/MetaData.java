package org.biomart.dino;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.biomart.common.utils.XMLElements;
import org.biomart.dino.annotations.Func;
import org.biomart.objects.objects.Element;
import org.biomart.queryEngine.QueryElement;

public class MetaData {

	static final XMLElements funcKey = XMLElements.FUNCTION;
	
	Map<String, QueryElement> boundEls = null;
	
	public MetaData() {}
	
	/**
	 * 
	 * @param fields fields bound to the query elements. From them we can retrieve the Function field values.
	 * @param qel QueryElements from the query, bound to dino fields.
	 */
	public void setBindings(List<Field> fields, List<QueryElement> qel) {
		boundEls = new HashMap<String, QueryElement>();
		Element e = null;
		Func a = null;
		String propVal = "";
		
		// TODO: improve performance
		for (QueryElement q : qel) {
			e = q.getElement();
			propVal = e.getPropertyValue(funcKey);
			for (Field f: fields) {
				a = f.getAnnotation(Func.class);
				if (a != null && a.id().equalsIgnoreCase(propVal)) {
					boundEls.put(propVal, q);
				}
			}
		}
	}
	
	/**
	 * 
	 * @return a map with 
	 * Key: the value of the Function field inside the configuration of the element,
	 * Value: the QueryElement that wraps this element.
	 * 
	 */
	public Map<String, QueryElement> getBindings() {
		return boundEls;
	}
}
