package org.biomart.dino;

import org.biomart.dino.controllers.DinoCtrl;
import org.biomart.queryEngine.Query;

public class Binder {

	public Binder(DinoCtrl ctrl, Query q) {
		ctrl.create(q);
	}
}
