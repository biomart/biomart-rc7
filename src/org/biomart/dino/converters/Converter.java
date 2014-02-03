package org.biomart.dino.converters;

import java.io.IOException;
import java.io.OutputStream;

import org.biomart.objects.objects.Attribute;

public interface Converter {

    public Converter run() throws IOException;
    
    public Converter setAttributes(Attribute attributeList);
    
    public Converter setFilter(String name, String value);
    
    public Converter setOutput(OutputStream out);
    
    
}
