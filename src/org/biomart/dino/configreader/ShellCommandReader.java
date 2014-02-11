package org.biomart.dino.configreader;

import java.util.Map;
import java.util.Properties;

public class ShellCommandReader extends ConfigReader {

    public ShellCommandReader(ConfigReader next) {
        super(next);
        // TODO Auto-generated constructor stub
    }

    public static final String BIN = "shellcommand.bin";

    @Override
    protected void putData(Properties props, Map<String, String> cfg) {
        String value = props.getProperty(BIN);
        
        cfg.put(BIN, value);
    }

}
