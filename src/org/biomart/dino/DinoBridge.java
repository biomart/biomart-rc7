package org.biomart.dino;

import org.biomart.dino.controllers.DinoCtrl;
import org.biomart.queryEngine.Query;

public class DinoBridge {

	public DinoBridge(Query q) {
		String dinoName = q.getDino();
		
		Class<? extends DinoCtrl> ctrlClass = getDinoCtrlClass(dinoName);
		
		DinoCtrl dinoCtrl = getDinoCtrlInstance(ctrlClass);
		
		new Binder(dinoCtrl, q);
	}
	
	protected Class<? extends DinoCtrl> 
	getDinoCtrlClass(String ctrlName) {
		return DinoCtrlRegistry.get(ctrlName);
	}
	
	protected DinoCtrl 
	getDinoCtrlInstance(Class<? extends DinoCtrl> klass) {
		// instanciate!
		return null;
	}
}
