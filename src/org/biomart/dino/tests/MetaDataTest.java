package org.biomart.dino.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.biomart.common.utils.XMLElements;
import org.biomart.dino.MetaData;
import org.biomart.objects.objects.Element;
import org.biomart.queryEngine.QueryElement;
import org.junit.Before;
import org.junit.Test;


public class MetaDataTest {
	
	List<Field> fields;
	List<QueryElement> qes;
	final String stAnn = "first", sdAnn = "second";
	Map<String, QueryElement> m;
	
	@Before
	public void setUp() {
//		Field class cannot be mocked :(

		Field[] fs = org.biomart.dino.tests.fixtures.MetaData.class.getDeclaredFields();
		Element e1 = mock(Element.class), e2 = mock(Element.class), 
				e3 = mock(Element.class);
		QueryElement q1 = mock(QueryElement.class), q2 = mock(QueryElement.class), 
				q3 = mock(QueryElement.class);
		
		when(q1.getElement()).thenReturn(e1);
		when(q2.getElement()).thenReturn(e2);
		when(q3.getElement()).thenReturn(e3);
		when(e1.getPropertyValue(XMLElements.FUNCTION)).thenReturn(stAnn);
		when(e2.getPropertyValue(XMLElements.FUNCTION)).thenReturn(sdAnn);
		when(e3.getPropertyValue(XMLElements.FUNCTION)).thenReturn("foo");
		
		fields = Arrays.asList(fs);
		qes = new ArrayList<QueryElement>();
		qes.add(q1);
		qes.add(q2);
		qes.add(q3);
		
		m = new HashMap<String, QueryElement>();
		m.put(stAnn, q1);
		m.put(sdAnn, q2);
	}
	
	@Test
	public void test() {
		MetaData md = new MetaData();
		
		assertFalse(fields.isEmpty());
		
		md.setBinding(fields, qes);
		Map<String, QueryElement> binding = md.getBinding();
		
		assertTrue(binding.size() != 0);
		assertEquals(m, binding);
		
//		Iterator<Map.Entry<String, QueryElement>> it = binding.entrySet().iterator();
//		while(it.hasNext()) {
//			Map.Entry<String, QueryElement> e = 
//					(Map.Entry<String, QueryElement>) it.next();
//			
//			assertTrue(m.containsKey(e.getKey()));
//			
//			assertEquals(m.get(e.getKey()).getElement(), e.getValue().getElement());
//		}
	}

}
