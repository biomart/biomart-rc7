package org.biomart.processors;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.RandomStringUtils;
import org.biomart.common.exceptions.BioMartQueryException;
import org.biomart.common.resources.Log;

import com.google.common.base.Function;
import com.google.common.base.Joiner;

import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.biomart.queryEngine.Query;
import org.biomart.queryEngine.QueryElement;


/**
 * Author: lpand AKA Luca Pandini
 *
 * ProcessorImpl already defined the proper ContentType (text/plain)
 */
public class Network extends ProcessorImpl {
    private DummyNetworkFunc cb;

    public Network() {
        Log.info("Network::Network called");
        cb = new DummyNetworkFunc();
    }

    @Override
    public void setQuery(Query query) {
        super.setQuery(query);

        // Now we must know how many attribute lists are within
        // the original query
        //String[] originalAttributes =
        //            getAttributeNames(query.getOriginalAttributeOrder());

        //cb = new DummyNetworkFunc(originalAttributes);
    }

    private String[] getAttributeNames(List<QueryElement> qe) {
        int qeSize = qe.size();
        String[] attrs = new String[qeSize];
        QueryElement e = null;

        for (int idx = 0; idx < qeSize; ++idx) {
            e = qe.get(idx);
            attrs[idx] = e.toString();
        }

        return attrs;
    }

    public Function getCallback() {
        return cb;
    }


    protected final class DummyNetworkFunc implements Function<String[],Boolean> {
        private static final int FLUSH_INTERVAL = 5;
        private int count = 0;
        Boolean served = false;

        final Random rand = new Random();
        final String annotations = "abcdefghilmo";

        public DummyNetworkFunc() {

        }

        public DummyNetworkFunc(String[] attrs) {
            this();
        }

        // private String makeAnnotationList() {
        //         String[] annotationList;
        //         int annotationListLenght, index = -1;

        //         annotationListLenght = rand.nextInt(6);
        //         annotationList = new String[annotationListLenght];
        //         while (index++ < annotationListLenght) {
        //             annotationList[index] =
        //                 RandomStringUtils.random(2, annotations);
        //         }

        //         Joiner.on('$').join(annotationList)
        // }

        
        

        
        @Override
        public Boolean apply(String[] row) {
            String line = Joiner.on('\t').join(row);
            try {
                out.write(line.getBytes());
                out.write(LINEFEED.getBytes());
                Log.debug("Line: "+count+" "+Joiner.on('/').join(row));

                // Force output to be written to client's stream
                if (++count % FLUSH_INTERVAL == 0) {
                    out.flush();
                }
            } catch (IOException e) {
                throw new BioMartQueryException("Problem writing to OutputStream", e);
            }
            return false; // Let QueryRunner decide when to stop
        }

    }


}
