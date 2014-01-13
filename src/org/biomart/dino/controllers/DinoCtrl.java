package org.biomart.dino.controllers;

import org.biomart.dino.dinos.Dino;
import org.biomart.queryEngine.Query;

public interface DinoCtrl {

	public abstract Dino create(Query query);
	
	
}
