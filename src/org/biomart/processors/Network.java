package org.biomart.processors;

import org.biomart.common.resources.Log;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import java.util.List;
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

    }

    @override
    public void setQuery(Query query) {
        super.setQuery(query);

        // Now we must know how many attribute lists are within
        // the original query
        String[] originalAttributes =
                    getAttributeNames(query.getOriginalAttributeOrder());

        cb = new DummyNetworkFunc(originalAttributes);
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

    public Function<String,Boolean> getCallback() {
        return cb
    }

    protected final class DummyNetworkFunc implements Function<String[],Boolean> {
        // Annotation term list
        private String[] attrs;
        private String[][] rowCache;
        private int cacheSize = 100;
        private int couter = -1;
        private String[][] dummyData

        public DummyNetworkFunc(String[] attrs) {
            this.attrs = attrs;
            rowCache = new String[cacheSize][]
        }

        private String[] giveMeDummyData(String)

        @Override
        public Boolean apply(String[] row) {
            if (++counter < cacheSize) {
                rowCache[counter] = row;
            } else if (counter == cacheSize) {
                dummyData = giveMeDummyData(rowCache);
                rowCache = null;
                super.DefaultWriteFunction writer =
                                            new super.DefaultWriteFunction();
                for (String[] s : dummyData) {
                    writer.apply(s)
                }
            } else {
                return false
            }

            return true
        }
    }

}
