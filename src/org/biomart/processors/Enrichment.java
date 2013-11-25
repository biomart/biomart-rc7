package org.biomart.processors;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import org.biomart.common.exceptions.BioMartQueryException;
import org.biomart.common.resources.Log;
import org.biomart.queryEngine.Query;
import org.biomart.queryEngine.QueryElement;
import org.jdom.Document;

import java.io.IOException;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: iwan
 * Date: 31/10/13
 * Time: 10:49
 * To change this template use File | Settings | File Templates.
 * @autor: iwan
 */
public class Enrichment extends ProcessorImpl {
    private EnrichmentWriteFunction cb;

    public Enrichment() {
        Log.debug("Enrichment called");
        cb = new EnrichmentWriteFunction();
    }

    public void setQuery(Query query) {
        super.setQuery(query);
    }


    protected final class EnrichmentWriteFunction implements Function<String[],Boolean> {
        private static final int FLUSH_INTERVAL = 5;
        private int count = 0;
        private boolean first_call = true;

                // {"cytoplasm", "0.7988158230958072", "ENSG00000152213,ENSG00000125877,ENSG00000160224,ENSG00000142168,ENSG00000132965,ENSG00000262563,ENSG00000102158,ENSG00000131055,ENSG00000131174,ENSG00000128340,ENSG00000165240,ENSG00000263298,ENSG00000100983,ENSG00000229215,ENSG00000008710,ENSG00000101306,ENSG00000164175,ENSG00000066455,ENSG00000102144,ENSG00000197888,ENSG00000164362,ENSG00000262577,ENSG00000164022,ENSG00000122859,ENSG00000096696,ENSG00000242110,ENSG00000262509,ENSG00000227129,ENSG00000198954,ENSG00000198689,ENSG00000164961,ENSG00000206440,ENSG00000087460,ENSG00000088367,ENSG00000187676,ENSG00000100225,ENSG00000148677,ENSG00000169567,ENSG00000111837,ENSG00000206505,ENSG00000111713,ENSG00000171564,ENSG00000235708,ENSG00000256062,ENSG00000167972,ENSG00000093010,ENSG00000184056,ENSG00000161970,ENSG00000111716,ENSG00000196586,ENSG00000206439,ENSG00000176225,ENSG00000263162,ENSG00000101577,ENSG00000198931,ENSG00000150753,ENSG00000100014,ENSG00000177542,ENSG00000008086,ENSG00000070061,ENSG00000198805,ENSG00000130921,ENSG00000103494,ENSG00000188906,ENSG00000005471,ENSG00000100526,ENSG00000088682,ENSG00000237412,ENSG00000151445,ENSG00000182899,ENSG00000145819,ENSG00000137812,ENSG00000114054,ENSG00000263007,ENSG00000103313,ENSG00000147465,ENSG00000110074,ENSG00000167792,ENSG00000111276,ENSG00000136492,ENSG00000157764,ENSG00000127990,ENSG00000169271,ENSG00000266173,ENSG00000164867,ENSG00000154099,ENSG00000170262,ENSG00000134333,ENSG00000130948,ENSG00000150275,ENSG00000169306,ENSG00000125744,ENSG00000152952,ENSG00000159640,ENSG00000161653,ENSG00000172270,ENSG00000101850,ENSG00000261577,ENSG00000074695,ENSG00000135677,ENSG00000141012,ENSG00000228978,ENSG00000181830,ENSG00000186847,ENSG00000164342,ENSG00000157911,ENSG00000022267,ENSG00000163541,ENSG00000112964,ENSG00000143315,ENSG00000168610,ENSG00000179520,ENSG00000135437,ENSG00000119509,ENSG00000162614,ENSG00000187535,ENSG00000108946,ENSG00000107537,ENSG00000018625,ENSG00000223957,ENSG00000204248,ENSG00000173801,ENSG00000228691,ENSG00000227315,ENSG00000223980,ENSG00000204386,ENSG00000204490,ENSG00000086848,ENSG00000147804,ENSG00000236346,ENSG00000234530,ENSG00000230108,ENSG00000109846,ENSG00000230930,ENSG00000234343,ENSG00000231834,ENSG00000184494,ENSG00000206290,ENSG00000128731,ENSG00000224320,ENSG00000235125,ENSG00000223952,ENSG00000234846,ENSG00000227801,ENSG00000235657,ENSG00000227565,ENSG00000228321,ENSG00000152254,ENSG00000073734,ENSG00000236196,ENSG00000228849,ENSG00000095585,ENSG00000204498,ENSG00000232810,ENSG00000206503,ENSG00000269036,ENSG00000269639,ENSG00000269083,ENSG00000269666"},
                // {"catabolic process", "0.10982515518276814", "ENSG00000125877,ENSG00000142168,ENSG00000128340,ENSG00000102144,ENSG00000242110,ENSG00000118972,ENSG00000100225,ENSG00000169567,ENSG00000093010,ENSG00000161970,ENSG00000136104,ENSG00000111716,ENSG00000263162,ENSG00000198805,ENSG00000188906,ENSG00000182899,ENSG00000114054,ENSG00000164867,ENSG00000134333,ENSG00000166035,ENSG00000159640,ENSG00000135677,ENSG00000141012,ENSG00000172987,ENSG00000107537,ENSG00000122512,ENSG00000128731,ENSG00000269666"},
                // {"chromosome organization", "0.31745784356933193", "ENSG00000125877,ENSG00000168769,ENSG00000101596,ENSG00000164362,ENSG00000102878,ENSG00000137812"},
                // {"nucleobase-containing compound catabolic process", "0.9483306370554542", "ENSG00000125877,ENSG00000142168,ENSG00000128340,ENSG00000169567,ENSG00000161970,ENSG00000136104,ENSG00000198805,ENSG00000188906,ENSG00000182899,ENSG00000122512"},
                // {"cell adhesion", "0.3344818047347162", "ENSG00000203618,ENSG00000236236,ENSG00000229353,ENSG00000011201,ENSG00000053747,ENSG00000234096,ENSG00000137197,ENSG00000008710,ENSG00000164022,ENSG00000096696,ENSG00000206439,ENSG00000127990,ENSG00000150275,ENSG00000169306,ENSG00000261577,ENSG00000102104,ENSG00000173801,ENSG00000236561,ENSG00000204490,ENSG00000230108,ENSG00000232641,ENSG00000237165,ENSG00000168477,ENSG00000230885,ENSG00000237834,ENSG00000237114,ENSG00000223952,ENSG00000234623,ENSG00000237123,ENSG00000137345,ENSG00000228321,ENSG00000228849,ENSG00000232810,ENSG00000204655"},
                // {"biosynthetic process", "0.740941709001749", "ENSG00000160224,ENSG00000184058,ENSG00000142168,ENSG00000132965,ENSG00000262563,ENSG00000102158,ENSG00000165240,ENSG00000263298,ENSG00000159216,ENSG00000100983,ENSG00000215612,ENSG00000168769,ENSG00000164175,ENSG00000102144,ENSG00000164362,ENSG00000262577,ENSG00000164022,ENSG00000242110,ENSG00000079215,ENSG00000087460,ENSG00000187676,ENSG00000169567,ENSG00000102878,ENSG00000111837,ENSG00000122691,ENSG00000111713,ENSG00000256062,ENSG00000214960,ENSG00000093010,ENSG00000161970,ENSG00000109132,ENSG00000206439,ENSG00000101577,ENSG00000261992,ENSG00000198931,ENSG00000070061,ENSG00000198805,ENSG00000130921,ENSG00000088682,ENSG00000151445,ENSG00000182899,ENSG00000134438,ENSG00000114054,ENSG00000107859,ENSG00000147465,ENSG00000110651,ENSG00000109101,ENSG00000164867,ENSG00000130948,ENSG00000166035,ENSG00000161653,ENSG00000101850,ENSG00000074695,ENSG00000163795,ENSG00000143315,ENSG00000124827,ENSG00000018625,ENSG00000234669,ENSG00000204490,ENSG00000086848,ENSG00000230108,ENSG00000264253,ENSG00000223858,ENSG00000232099,ENSG00000226858,ENSG00000223952,ENSG00000223852,ENSG00000228321,ENSG00000206510,ENSG00000152254,ENSG00000073734,ENSG00000228849,ENSG00000232810,ENSG00000143614,ENSG00000204644,ENSG00000269639,ENSG00000269666"}


        private final String[][] dummy_rows = new String[][] {
                {"Annotation", "Score", "Gene List"},
                {"cytoplasm", "0.7988158230958072", "ENSG00000152213,ENSG00000125877,ENSG00000160224,ENSG00000142168,ENSG00000132965,ENSG00000262563,ENSG00000102158,ENSG00000131055,ENSG00000131174,ENSG00000128340,ENSG00000165240,ENSG00000263298,ENSG00000100983,ENSG00000229215,ENSG00000008710,ENSG00000101306,ENSG00000164175,ENSG00000066455,ENSG00000102144,ENSG00000197888,ENSG00000164362,ENSG00000262577,ENSG00000164022,ENSG00000122859,ENSG00000096696,ENSG00000242110,ENSG00000262509,ENSG00000227129,ENSG00000198954,ENSG00000198689,ENSG00000164961,ENSG00000206440,ENSG00000087460,ENSG00000088367,ENSG00000187676,ENSG00000100225,ENSG00000148677,ENSG00000169567,ENSG00000111837,ENSG00000206505,ENSG00000111713,ENSG00000171564,ENSG00000235708,ENSG00000256062,ENSG00000167972,ENSG00000093010,ENSG00000184056,ENSG00000161970,ENSG00000111716,ENSG00000196586,ENSG00000206439,ENSG00000176225,ENSG00000263162,ENSG00000101577,ENSG00000198931,ENSG00000150753,ENSG00000100014,ENSG00000177542,ENSG00000008086,ENSG00000070061,ENSG00000198805,ENSG00000130921,ENSG00000103494,ENSG00000188906,ENSG00000005471,ENSG00000100526,ENSG00000088682,ENSG00000237412,ENSG00000151445,ENSG00000182899,ENSG00000145819,ENSG00000137812,ENSG00000114054,ENSG00000263007,ENSG00000103313,ENSG00000147465,ENSG00000110074,ENSG00000167792,ENSG00000111276,ENSG00000136492,ENSG00000157764,ENSG00000127990,ENSG00000169271,ENSG00000266173,ENSG00000164867,ENSG00000154099,ENSG00000170262,ENSG00000134333,ENSG00000130948,ENSG00000150275,ENSG00000169306,ENSG00000125744,ENSG00000152952,ENSG00000159640,ENSG00000161653,ENSG00000172270,ENSG00000101850,ENSG00000261577,ENSG00000074695,ENSG00000135677,ENSG00000141012,ENSG00000228978,ENSG00000181830,ENSG00000186847,ENSG00000164342,ENSG00000157911,ENSG00000022267,ENSG00000163541,ENSG00000112964,ENSG00000143315,ENSG00000168610,ENSG00000179520,ENSG00000135437,ENSG00000119509,ENSG00000162614,ENSG00000187535,ENSG00000108946,ENSG00000107537,ENSG00000018625,ENSG00000223957,ENSG00000204248,ENSG00000173801,ENSG00000228691,ENSG00000227315,ENSG00000223980,ENSG00000204386,ENSG00000204490,ENSG00000086848,ENSG00000147804,ENSG00000236346,ENSG00000234530,ENSG00000230108,ENSG00000109846,ENSG00000230930,ENSG00000234343,ENSG00000231834,ENSG00000184494,ENSG00000206290,ENSG00000128731,ENSG00000224320,ENSG00000235125,ENSG00000223952,ENSG00000234846,ENSG00000227801,ENSG00000235657,ENSG00000227565,ENSG00000228321,ENSG00000152254,ENSG00000073734,ENSG00000236196,ENSG00000228849,ENSG00000095585,ENSG00000204498,ENSG00000232810,ENSG00000206503,ENSG00000269036,ENSG00000269639,ENSG00000269083,ENSG00000269666"},
                {"chromosome organization", "0.31745784356933193", "ENSG00000125877,ENSG00000168769,ENSG00000101596,ENSG00000164362,ENSG00000102878,ENSG00000137812"},
                {"histone binding", "0.7193795386042564", "ENSG00000160224"},
                {"vesicle-mediated transport", "0.8017489377457154", "ENSG00000142168,ENSG00000066455,ENSG00000164022,ENSG00000165471,ENSG00000171564,ENSG00000184056,ENSG00000196586,ENSG00000074695,ENSG00000112964"},
                {"membrane organization", "0.6099332903629029", "ENSG00000142168,ENSG00000184056,ENSG00000196586,ENSG00000169306,ENSG00000168621"},
                {"cellular amino acid metabolic process", "0.9918407265668878", "ENSG00000142168,ENSG00000165240,ENSG00000100983,ENSG00000164022,ENSG00000079215,ENSG00000164867,ENSG00000161653,ENSG00000112964"},
                {"sulfur compound metabolic process", "0.5803235571022848", "ENSG00000142168,ENSG00000100983,ENSG00000114054,ENSG00000135677,ENSG00000141012,ENSG00000112964"}
        };

        @Override
        public Boolean apply(String[] row) {
            if (first_call) {
                int rows_num = dummy_rows.length;
                for (String[] r : dummy_rows) {
                    sub_apply(r);
                }
                first_call = false;
            }
            return false; // Let QueryRunner decide when to stop
        }

        public Boolean sub_apply(String[] row) {

            String line = Joiner.on('\t').join(row);
            try {
                out.write(line.getBytes());
                out.write(LINEFEED.getBytes());

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

    private String[] getAttributeNames(List<QueryElement> qe) {
        String[] attr = {"Annotation", "Score", "Gene list"};
        return attr;
    }

    @Override
    public Function getCallback() {
        return new EnrichmentWriteFunction();
    }
}