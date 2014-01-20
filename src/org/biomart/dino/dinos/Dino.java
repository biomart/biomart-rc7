package org.biomart.dino.dinos;

import java.io.OutputStream;
import org.biomart.queryEngine.Query;

public interface Dino {

	public void run(OutputStream out);
	public Dino setQuery(Query query);
	public Dino setMimes(String[] mimes);
}
