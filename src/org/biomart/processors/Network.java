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
        /**
         * NOTE
         *
         * To make the IframeOutputStream (which is the only stream used with
         * requests by the front-end since it's hardcoded within it)
         * happy we MUST send atleast 50 lines, otherwise it won't work properly.
         *
         * I'm appending a newline at each line because write(NEWLINE) seems
         * not working well (as it's used inside the DefaultWriteFunction).
         */
        public Boolean apply(String[] row) {
            // Log.info("NetworkProcessor::DummyNetworkFunc#apply called");
            if (!served) {
                String[][] rows = new String[][] {
                    {"gene1", "gene2", "weight"},
                    {"ENSG00000003756", "ENSG00000005075", "7.5E-3"},
                    {"ENSG00000002822", "ENSG00000007168", "9.2E-3"},
                    {"ENSG00000005249", "ENSG00000007168", "9.7E-3"},
                    {"ENSG00000004897", "ENSG00000008018", "1.9E-2"},
                    {"ENSG00000005339", "ENSG00000008838", "1.9E-2"},
                    {"ENSG00000006125", "ENSG00000010610", "7.5E-2"},
                    {"ENSG00000006607", "ENSG00000010810", "7.2E-2"},
                    {"ENSG00000005075", "ENSG00000011007", "1.4E-2"},
                    {"ENSG00000003756", "ENSG00000011304", "1.1E-2"},
                    {"ENSG00000005075", "ENSG00000011304", "7.5E-3"},
                    {"ENSG00000005075", "ENSG00000012061", "1.4E-2"},
                    {"ENSG00000008018", "ENSG00000013275", "1.8E-2"},
                    {"ENSG00000008441", "ENSG00000013503", "7.6E-2"},
                    {"ENSG00000006634", "ENSG00000014138", "2.9E-2"},
                    {"ENSG00000002330", "ENSG00000015475", "2.5E-1"},
                    {"ENSG00000005075", "ENSG00000020426", "6.6E-3"},
                    {"ENSG00000011007", "ENSG00000020426", "1.9E-2"},
                    {"ENSG00000012061", "ENSG00000020426", "1.9E-2"},
                    {"ENSG00000004779", "ENSG00000023228", "2.3E-2"},
                    {"ENSG00000013503", "ENSG00000023608", "4.2E-2"},
                    {"ENSG00000001084", "ENSG00000023909", "1E0"},
                    {"ENSG00000002822", "ENSG00000030066", "8.9E-3"},
                    {"ENSG00000007168", "ENSG00000030066", "5.7E-3"},
                    {"ENSG00000015475", "ENSG00000030110", "6.3E-1"},
                    {"ENSG00000002822", "ENSG00000031691", "1.2E-2"},
                    {"ENSG00000007168", "ENSG00000031691", "7.8E-3"},
                    {"ENSG00000030066", "ENSG00000031691", "7.6E-3"},
                    {"ENSG00000014138", "ENSG00000035928", "4E-2"},
                    {"ENSG00000005249", "ENSG00000037042", "1.6E-2"},
                    {"ENSG00000007168", "ENSG00000037042", "9.4E-3"},
                    {"ENSG00000005007", "ENSG00000037241", "1.4E-2"},
                    {"ENSG00000021852", "ENSG00000039537", "1.7E-1"},
                    {"ENSG00000002822", "ENSG00000040275", "1.4E-2"},
                    {"ENSG00000007168", "ENSG00000040275", "9.2E-3"},
                    {"ENSG00000030066", "ENSG00000040275", "8.9E-3"},
                    {"ENSG00000031691", "ENSG00000040275", "1.2E-2"},
                    {"ENSG00000004897", "ENSG00000041357", "1.9E-2"},
                    {"ENSG00000008018", "ENSG00000041357", "1.7E-2"},
                    {"ENSG00000013275", "ENSG00000041357", "1.8E-2"},
                    {"ENSG00000005339", "ENSG00000042429", "1.9E-2"},
                    {"ENSG00000008838", "ENSG00000042429", "1.9E-2"},
                    {"ENSG00000006125", "ENSG00000042753", "5.1E-2"},
                    {"ENSG00000010610", "ENSG00000042753", "7.8E-2"},
                    {"ENSG00000015285", "ENSG00000043462", "8.3E-1"},
                    {"ENSG00000005249", "ENSG00000046651", "1.6E-2"},
                    {"ENSG00000007168", "ENSG00000046651", "9.7E-3"},
                    {"ENSG00000037042", "ENSG00000046651", "1.6E-2"},
                    {"ENSG00000088986", "ENSG00000116127", "1.6E-2"},
                    {"ENSG00000101004", "ENSG00000116127", "1.6E-2"},
                    {"ENSG00000101367", "ENSG00000116127", "9.5E-3"},
                    {"ENSG00000101624", "ENSG00000116127", "1.6E-2"},
                    {"ENSG00000101639", "ENSG00000116127", "1.6E-2"},
                    {"ENSG00000103540", "ENSG00000116127", "1.6E-2"},
                    {"ENSG00000103995", "ENSG00000116127", "1.6E-2"},
                    {"ENSG00000105568", "ENSG00000116127", "9.2E-3"},
                    {"ENSG00000106477", "ENSG00000116127", "1.6E-2"},
                    {"ENSG00000108953", "ENSG00000116127", "1.6E-2"}
                };

                String line;
                int count = 0;
                for (String[] r : rows) {
                    line =  Joiner.on('\t').join(r) + "\n";
                    try {
                        out.write(line.getBytes());
                        // out.write(NEWLINE);

                        // Force output to be written to client's stream
                        if (++count % 5 == 0) {
                            out.flush();
                        }
                    } catch (IOException e) {
                        throw new BioMartQueryException("Problem writing to OutputStream", e);
                    }
                }

                served = true;
            }

            return false;
        }
    }


}
