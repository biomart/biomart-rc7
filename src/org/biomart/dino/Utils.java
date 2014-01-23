package org.biomart.dino;

import java.util.List;

import org.biomart.objects.objects.Attribute;
import org.biomart.objects.objects.Element;
import org.biomart.queryEngine.QueryElement;

public class Utils {

	/**
	 * Given an Attribute element e, it returns the attribute within the
	 * attribute list e belongs to useful for any id to Ensembl id.
	 * 
	 * @param e attribute where retrieve the attribute list from.
	 * @return an attribute to use for Ensembl id translation or the input attribute if the operation failed.
	 */
	public static Attribute 
	getAttributeForTranslation(Attribute e) {
		List<Attribute> eList = e.getAttributeList();
		return eList.isEmpty() ? e : eList.get(0);
	}
	
	/**
	 * Given an Attribute element e, it returns the attribute within the
	 * attribute list e belongs to useful for into human specie translation.
	 * 
	 * @param e attribute where retrieve the attribute list from.
	 * @return an attribute to use into human specie translation or the input attribute if the operation failed.
	 */
	public static Attribute
	getAttributeForSpeciesTranslation(Attribute e) {
		List<Attribute> eList = e.getAttributeList();
		return eList.size() > 1 ? eList.get(1) : e;
	}
	
	public static String
	getDatasetName(Attribute e) {
		String s = e.getPointedDatasetName();
		return s == null ? "" : s;
	}
	
	public static String
	getDatasetConfig(Element e) {
		String s = e.getPointedConfigName();
		return s == null ? "" : s;
	}
	
	public static String
	getDatasetName(QueryElement e) {
		String s = e.getDataset().getName();
		return s == null ? "" : s;
	}
	
}
