package org.biomart.dino.configreader;

import java.io.File;

public interface IConfigReader {

    public String get(String key);
    
    public IConfigReader setConfigFile(File configFile);
}
