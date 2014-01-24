package org.biomart.dino.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.biomart.common.utils.XMLElements;
import org.biomart.dino.DinoHandler;
import org.biomart.dino.tests.fixtures.TestDino;
import org.biomart.objects.objects.Element;
import org.biomart.dino.tests.TestSupport;
import org.biomart.dino.annotations.Func;
import org.biomart.dino.dinos.Dino;
import org.biomart.queryEngine.QueryElement;
import org.biomart.queryEngine.QueryElementType;
import org.junit.Test;

public class DinoHandlerTest {
	final String dinoClassName = "org.biomart.dino.tests.fixtures.TestDino";
	final Class<TestDino> testDinoClass = TestDino.class;

	@Test
	public void getDinoClassTest() {
		try {
			Class<? extends Dino> klass = DinoHandler.getDinoClass(dinoClassName);
			assertEquals("returned the proper class", dinoClassName, klass.getName());
		} catch (ClassNotFoundException e) {
			fail("getDinoClass has raised an exception");
		}
	}
	
	@Test(expected = ClassNotFoundException.class)
	public void getDinoClassNotFoundTest() throws ClassNotFoundException {
		String className = "wat";
		@SuppressWarnings("unused")
		Class<? extends Dino> klass = DinoHandler.getDinoClass(className);
	}
	
	@Test(expected = RuntimeException.class)
	public void getDinoClassCastFailTest() throws ClassNotFoundException {
		String className = "java.lang.String";
		@SuppressWarnings("unused")
		Class<? extends Dino> klass = DinoHandler.getDinoClass(className);
	}
	
	@Test
	public void getDinoInstanceTest() {
		Dino dino = null;
		try {
			dino = DinoHandler.getDinoInstance(TestDino.class);
		} catch (IllegalArgumentException | InstantiationException
				| IllegalAccessException | InvocationTargetException e) {
			fail(e.getMessage());
		}
		assertEquals("returns the proper instance", dino.getClass(), testDinoClass);
	}
	
	@Test
	public void getAnnotatedFieldsTest() {
		List<Field> fields = DinoHandler.getAnnotatedFields(testDinoClass);
		assertEquals("only two fields are annotated", 2, fields.size());
		Func f = fields.get(0).getAnnotation(Func.class),
			 s = fields.get(1).getAnnotation(Func.class);
		assertEquals("first", f.id()); assertFalse(f.optional());
		assertEquals("second", s.id()); assertTrue(s.optional());
	}

	@Test
	public void setFieldValuesTest() throws IllegalArgumentException, IllegalAccessException {
		String otherFilter = "other filter", otherAttribute = "other attribute",
				first = "first", second = "second", stVal = "1st";
		Element e = null;
		QueryElement qe;
		TestDino dino = new TestDino();
		List<Field> fds = DinoHandler.getAnnotatedFields(testDinoClass);
		List<QueryElement> expectedBoundEls = new ArrayList<QueryElement>(),
				boundEls = null;
		
		
		List<QueryElement> l = new ArrayList<QueryElement>();
		
		// I'm adding these to have a more generic case
		e = TestSupport.mockAttributeElement(XMLElements.FUNCTION, otherAttribute, otherAttribute);
		l.add(TestSupport.mockQE(e, QueryElementType.ATTRIBUTE));
 
		e = TestSupport.mockFilterElement(XMLElements.FUNCTION, otherFilter, null);
		// notice that i'm not mocking the getFilterValues method here.
		l.add(TestSupport.mockQE(e, QueryElementType.FILTER));
		
		e = TestSupport.mockAttributeElement(XMLElements.FUNCTION, second, second);
        l.add(qe = TestSupport.mockQE(e, QueryElementType.ATTRIBUTE));
        expectedBoundEls.add(qe);
        
		e = TestSupport.mockAttributeElement(XMLElements.FUNCTION, first, null);
		l.add(qe = TestSupport.mockQE(e, QueryElementType.FILTER));
		when(qe.getFilterValues()).thenReturn(stVal);
		expectedBoundEls.add(qe);
		
		boundEls = DinoHandler.setFieldValues(dino, fds, l);
		
		assertEquals(stVal, dino.getF1());
		assertEquals(second, dino.getF2());
		
		Iterator<QueryElement> it = boundEls.iterator();
		while(it.hasNext()) {
			qe = it.next();
			assertTrue(expectedBoundEls.contains(qe));
		}
		
//		assertEquals("it returned the proper QueryElement elements", expectedBoundEls, boundEls);
	}
}




























