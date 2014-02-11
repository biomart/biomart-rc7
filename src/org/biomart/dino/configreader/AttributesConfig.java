package org.biomart.dino.configreader;

import java.util.Map;
import java.util.Properties;

public class AttributesConfig extends ConfigReader {

    public AttributesConfig() {}
    
    public AttributesConfig(ConfigReader next) {
        super(next);
        // TODO Auto-generated constructor stub
    }

    public static final String sep = System.getProperty("file.separator");
    public static final String ATTR = "attributes";

//    @Inject
//    public AttributesConfig(@Named("Dino Properties Directory") String confDirPath) {
//        this.confDirPath = confDirPath;
//    }


    /**
     * Keys of the map are name of columns of data whereas values are BioMart's
     * attribute names.
     * 
     * @param fileName
     *            Just the file name of the file inside the configuration
     *            folder.
     * @return Returns a map of (attribute display name, attribute internal
     *         name) or an empty map if there's been any error while reading.
     */
    @Override
    public void putData(Properties props, Map<String, String> cfg) {

        String attributes = props.getProperty(ATTR),
               entries[] = attributes.split(",");

        for (int i = 0; i < entries.length; ++i) {
            entries[i] = entries[i].trim();
        }

        for (String s : entries) {
            String[] kv = s.split("=");
            cfg.put(kv[0].trim(), kv[1].trim());
        }
    }

    /**
     * Given a string of the form "k0=v0, k1 = v1, ...", it returns a map.
     * 
     * @param attributes
     * @return
     */

}
