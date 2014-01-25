package org.biomart.dino.dinos;

import java.io.IOException;
import java.io.OutputStream;

import org.biomart.dino.Binding;
import org.biomart.queryEngine.Query;

public interface Dino {

	public void run(OutputStream out) throws IOException;
	public Dino setQuery(Query query);
	public Dino setMetaData(Binding metaData);
	public Dino setMimes(String[] mimes);
}
