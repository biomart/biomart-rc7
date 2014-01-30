package org.biomart.dino.configreader;

import java.util.Map;
import java.util.Properties;

public abstract class ConfigReader {
    
    protected ConfigReader next;
    
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
    
    protected abstract void putData(Properties props, Map<String, String> cfg);

}
