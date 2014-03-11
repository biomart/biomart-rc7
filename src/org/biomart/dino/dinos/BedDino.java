package org.biomart.dino.dinos;

import java.io.OutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.biomart.dino.Binding;
import org.biomart.dino.annotations.Func;
import org.biomart.queryEngine.Query;

public class BedDino implements Dino {

    static public final String BACKGROUND = "background",
            SETS = "sets",
            BEDFILE = "bedfile",
            ANNOTATION = "annotation",
            CUTOFF = "cutoff",
            BONF = "bonferroni";
    
    @Func(id = BACKGROUND, optional = true)
    String background;
    @Func(id = SETS, optional = true)
    String sets;
    @Func(id = BEDFILE, optional = true)
    String bedFile;
    @Func(id = ANNOTATION)
    String annotation;
    @Func(id = CUTOFF)
    String cutoff;
    @Func(id = BONF, optional = true)
    String bonferroni;
    
    @Override
    public void run(OutputStream out) throws Exception {
        // TODO Auto-generated method stub

    }

    @Override
    public Dino setQuery(Query query) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Dino setMetaData(Binding metaData) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Dino setMimes(String[] mimes) {
        // TODO Auto-generated method stub
        return null;
    }
    
    
    /*
     * Expected format of the input:
     * 
     * chr<string>\t<integer>\t<integer>
     * 
     * or
     * 
     * chr<string>\t<integer>\t<integer>\t<string>\t<integer>\t<string>
     * 
     * where, in the latter case the last column is the strand and the 4th and
     * 5th can be empty.
     * 
     * The output will be
     * 
     * <string>:<integer>:<integer>
     * 
     * or
     * 
     * <string>:<integet>:<integer>:<string>
     * 
     * where the last column is the strand
     */
    public String getEnsemblFormat(String bed) {
        // Groups:
        // 0: entire expression
        // 1: (chr) thing
        // 2: (\\S+) the name of the chromosome 
        // 3: (\\d+) start
        // 4: (\\d+) end
        // 5: all the optional columns
        // 6: the strand
        //  "(chr)?(\\S+)\t(\\d+)\t(\\d+)(\t\\S*\t\\d*(["+Pattern.quote("+")+"-]))?"
        String regex = "(chr)?(\\S+)\t(\\d+)\t(\\d+)(\t\\S*\t\\S*\t([-\\+]))?",
               lines[] = null, name, start, end, strand, od = ":", rd = ",";
        StringBuilder out = new StringBuilder();
        
        Pattern p = Pattern.compile(regex);
        Matcher m = null;
        lines = bed.split("\n");
        
        for (int i = 0, len = lines.length; i < len; ++i) {
            m = p.matcher(lines[i]);
            if (m.matches()) {
                name = m.group(2);
                start = m.group(3);
                end = m.group(4);
                out.append(name)
                    .append(od)
                    .append(start)
                    .append(od)
                    .append(end);
                if (m.group(6) != null) {
                    strand = m.group(6);
                    out.append(od).append(strand);
                }
                if (i < len - 1) {
                    out.append(rd);
                }
            }
        }
        
        
        return out.toString();
    }

}

































































































