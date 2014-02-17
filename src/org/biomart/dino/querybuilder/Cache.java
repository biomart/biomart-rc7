package org.biomart.dino.querybuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.base.Joiner;

public class Cache {

    private ConcurrentHashMap<String, String> c;
    private QueryBuilder qb;
    private String header, colDelim = "\t", lineDelim = "\n";
    
    /**
     * In the data returned by the query builder call the first column
     * is assumed to be the key column and the others data.
     * 
     * @param qb
     * @throws IOException
     */
    public Cache(QueryBuilder qb) throws IOException {
        c = new ConcurrentHashMap<String, String>();
        this.qb = qb;
        getResults();
    }
    
    
    private void getResults() throws IOException {
        try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            qb.getResults(out);
            
            String lines[] = out.toString().split(lineDelim), tks[];
            out.reset();
            
            if (lines.length > 0) {
                tks = lines[0].split(colDelim);
                header = getData(tks);
                
                for (int i = 1, len = lines.length; i < len; ++i) {
                    String line = lines[i];
                    tks = line.split(colDelim);
                    if (tks.length > 0)
                        c.put(tks[0], getData(tks));
                }
            }
        }
    }
    
    
    public String getColDelim() {
        return colDelim;
    }


    public void setColDelim(String colDelim) {
        this.colDelim = colDelim;
    }


    public String getLineDelim() {
        return lineDelim;
    }


    public void setLineDelim(String lineDelim) {
        this.lineDelim = lineDelim;
    }


    public String get(String key) {
        return c.get(key);
    }
    
    
    public String getHeader() {
        return header;
    }
    
    
    private String getData(String[] tokens) {
        return Joiner.on(colDelim).join((Arrays.copyOfRange(tokens, 1, tokens.length)));
    }
    
    
}



































































