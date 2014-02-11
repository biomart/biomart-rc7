package org.biomart.dino.command;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.StringTokenizer;

import org.biomart.common.resources.Log;

public class HypgRunner extends ShellRunner {

    /**
     * It parses the output file, hypg.pv, living in the working directory,
     * and returns a __sorted__ List of string arrays.
     * 
     * Results are sorted in increasing order based on p-value and filtered: 
     * only the first 50 results are returned.
     * 
     * The format of a line is: ["annotation", "p-value", "bp-value", "g0,g1,..."]
     * where the last item can be missing.
     * 
     * @throws IOException 
     * 
     */
    @Override
    public Object getResults() throws IOException {
        // With the current hypg.exe program, the output will be in the
        // hypg.pv file in the folder the bin has been run.

        // Input format
        // string float float string*
        
        File fin = new File(this.dir, "hypg.pv");
        List<List<String>> results = new ArrayList<List<String>>();
        StringTokenizer st;
        
        try (BufferedReader in = new BufferedReader(new FileReader(fin))) {
            List<String> row;
            String line = null;
        
            while((line = in.readLine()) != null) {
                st = new StringTokenizer(line);
                
                if (st.countTokens() < 3) {
                    Log.error(this.getClass().getName()
                        + "#getResults() bad input: "+ line);
                    continue;
                }
                
                row = new ArrayList<String>(4);
                
                // Annotation   p-value bonferroni_p-value
                for (int i = 0; i < 3; ++i) { row.add(st.nextToken()); }
                
                StringBuilder sb = new StringBuilder();
                while(st.hasMoreTokens()) {
                    sb.append(st.nextToken());
                    sb.append(',');
                }
                
                sb.deleteCharAt(sb.length() - 1);
                row.add(sb.toString());
                results.add(row);
            }
            
        } catch (FileNotFoundException e) {
            Log.error(this.getClass().getName()
                    + "#getResults() cannot find inputfile hypg.pv");
            throw e;
        }
        
        Collections.sort(results, new Comparator<List<String>>() {
            @Override
            public int compare(List<String> a, List<String> b) {
                return a.get(0).compareTo(b.get(0));
            }
        });
        
        return results;
    }

}





//public Object getResults() {
//    // With the current hypg.exe program, the output will be in the
//    // hypg.pv file in the folder the bin has been run.
//
//    // Input format
//    // string float float string*
//    
//    int resultsLimit = 50, count = 0;
//    File fin = new File(this.dir, "hypg.pv");
//    SortedSet<String[]> results = new TreeSet<String[]>(
//        new Comparator<String[]>() {
//            @Override
//            public int compare(String[] a, String[] b) {
//                return a[1].compareTo(b[1]);
//            }
//        }
//    );
//    
//    try (BufferedReader in = new BufferedReader(new FileReader(fin))) {
//        String[] tokens = null;
//        String line = null;
//        while(count < resultsLimit && (line = in.readLine()) != null) {
//            tokens = line.trim().split("\t");
//            
//            if (tokens.length < 3) {
//                Log.error(this.getClass().getName()
//                    + "#getResults() bad input: "+ line);
//                continue;
//            }
//            
//            for (int i = 0; i < tokens.length; ++i) {
//                // Ignoring possibility of empty items
//                tokens[i] = tokens[i].trim();
//            }
//            results.add(tokens);
//        }
//        
//    } catch (FileNotFoundException e) {
//        // TODO Auto-generated catch block
//        e.printStackTrace();
//    } catch (IOException e) {
//        // TODO Auto-generated catch block
//        e.printStackTrace();
//    }
//    
//    return results;
//}

