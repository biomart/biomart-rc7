package org.biomart.dino.configreader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public abstract class ConfigReader {
    
    protected ConfigReader next;
    protected File configFile;
    
    public ConfigReader() {}
    
    public ConfigReader(ConfigReader next) {
        this.next = next;
    }

    public void read(Properties props, Map<String, String> cfg) {
        this.putData(props, cfg);
        if (this.next != null) {
            this.next.read(props, cfg);
        }
    }
    
    public Map<String, String> getConfig() throws FileNotFoundException, IOException {
        
        Properties pp = new Properties();
        Map<String, String> cfg = new HashMap<String, String>();
        
        try(FileInputStream inStream = new FileInputStream(configFile)) {
            pp.load(inStream);
        }
        
        this.read(pp, cfg);
        
        return cfg;
    }
    
    protected abstract void putData(Properties props, Map<String, String> cfg);
    
    public ConfigReader setConfigFile(String path) {
        configFile = new File(path);
        
        return this;
    }

}
