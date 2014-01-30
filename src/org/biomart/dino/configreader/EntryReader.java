package org.biomart.dino.configreader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class EntryReader extends ConfigReader {
    protected Properties pp;
    protected Map<String, String> cfg;
    
    public EntryReader(ConfigReader next) {
        super(next);
        
        pp = new Properties();
        cfg = new HashMap<String, String>();
    }

    @Override
    protected void putData(Properties props, Map<String, String> cfg) {
        // TODO Auto-generated method stub

    }
    
    public EntryReader setConfigFile(String path) throws FileNotFoundException, IOException {
        File configFile = new File(path);
        
        try(FileInputStream inStream = new FileInputStream(configFile)) {
            pp.load(inStream);
        }
        
        return this;
    }
    
    public Map<String, String> getConfig() {
        this.read(pp, cfg);
        
        return cfg;
    }

}
