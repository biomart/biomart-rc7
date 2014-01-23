package org.biomart.dino.dinos;

import java.io.OutputStream;

import org.biomart.dino.MetaData;
import org.biomart.queryEngine.Query;

public interface Dino {

	public void run(OutputStream out);
	public Dino setQuery(Query query);
	public Dino setMetaData(MetaData metaData);
	public Dino setMimes(String[] mimes);
}
