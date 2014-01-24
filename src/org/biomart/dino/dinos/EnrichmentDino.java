package org.biomart.dino.dinos;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.biomart.common.resources.Log;
import org.biomart.dino.MetaData;
import org.biomart.dino.Utils;
import org.biomart.dino.annotations.Func;
import org.biomart.dino.command.Command;
import org.biomart.dino.command.CommandRunner;
import org.biomart.dino.command.HypgCommand;
import org.biomart.dino.command.HypgRunner;
import org.biomart.dino.querybuilder.QueryBuilder;
import org.biomart.objects.objects.Attribute;
import org.biomart.objects.objects.Element;
import org.biomart.queryEngine.Query;
import org.biomart.queryEngine.QueryElement;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * NOTE This implementation assumes the incoming query has only one attribute!
 * 
 * @author luca
 * 
 */
public class EnrichmentDino implements Dino {
    static public final String BACKGROUND = "background", SETS = "sets",
            ANNOTATION = "annotation", CUTOFF = "cutoff";

    // Key: dataset_config_name + attribute_list name
    // Value: path of the annotation file
    static private Map<String, String> annotationsFilePaths = 
            new HashMap<String, String>();

    @Func(id = BACKGROUND)
    String background;
    @Func(id = SETS)
    String sets;
    @Func(id = ANNOTATION)
    String annotation;
    @Func(id = CUTOFF)
    String cutoff;

    String client;
    Query q;

    HypgCommand cmd;
    HypgRunner cmdRunner;
    QueryBuilder qbuilder;
    MetaData metadata;

    // Temporary files.
    File backgroundInput, setsInput;

    @Inject
    public EnrichmentDino(HypgCommand cmd, HypgRunner cmdRunner,
            @Named("JavaApi") QueryBuilder qbuilder) {
        this.cmd = cmd;
        this.cmdRunner = cmdRunner;
        this.qbuilder = qbuilder;
    }

    @Override
    public void run(OutputStream out) throws IOException {
        Log.debug(this.getClass().getName() + "#run(OutputStream) invoked");
        
        translateFilters();
        String annFile = getAnnotationsFilePath(annotation);
        
        try {
            if (annFile.isEmpty()) {
                throw new IOException("Cannot find annotations file nor retrieve them");
            }
            
            if (q.getClient().equalsIgnoreCase("false")) {
                // serve plain results
                // The bin path is sat within the DinoModule as a constant.
                cmd.setAnnotations(new File(annFile))
                    .setBackground(backgroundInput)
                    .setSets(setsInput)
                    .setCutoff(cutoff);
    
                @SuppressWarnings("unchecked")
                List<String[]> results = (List<String[]>) cmdRunner.setCmd(cmd)
                        .run()
                        .getResults();
            } else {
                // serve app
            }
            
        } catch (IOException e) {
            throw e;
        } catch (InterruptedException e) {
            Log.error(this.getClass().getName() + "enrichment interrupted ", e);
            throw new IOException(e);
        } finally {
            backgroundInput.delete();
            setsInput.delete();
        }
        

    }

 // TODO: print the header if requested.
    public void sendResults(List<String[]> res, OutputStream o) throws IOException {
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(o));
        for (String[] a : res) {
            for (String s : a) {
                writer.write(s, 0, s.length());
            }
            writer.newLine();
        }
    }
    
    // TODO: print the header if requested.
    public void sendResults(InputStream i, OutputStream o) throws IOException {
        // use stream pipeline
    }
    
    public void translateFilters() throws IOException {
        String[] filters = new String[] { background, sets };
        File[] inputFiles = new File[] { backgroundInput, setsInput };
        String filterName = "", filt;
        Map<String, QueryElement> bind = this.metadata.getBindings();
        QueryElement qe;

        for (int i = 0; i < filters.length; ++i) {
            filt = filters[i];
            // These files must be deleted at the end of processing.
            File f = inputFiles[i] = File.createTempFile(filt, "filter");

            try (FileOutputStream oStream = new FileOutputStream(f)) {

                qe = bind.get(filt);
                filterName = qe.getElement().getName();
                toEnsemblGeneId(annotation, filterName, qe.getFilterValues(),
                        oStream);

            } catch (FileNotFoundException e) {
                Log.error(this.getClass().getName() + "#translateFilters() "
                        + "impossible to write on temporary file.", e);
                throw e;
            }
        }

    }

    /**
     * It returns the absolute path to the annotation file if already present.
     * Otherwise, it gathers and put them on disk as temporary file.
     * 
     * @param attributeList
     * @return a path or empty list if something went wrong.
     */
    public String getAnnotationsFilePath(String attributeList) {
        // 1. check if already present on disk
        QueryElement qe = this.metadata.getBindings().get(attributeList);
        Attribute attr = Utils.getAttributeForAnnotationRetrieval(qe);
        String datasetConfigName = Utils.getDatasetConfig(qe.getElement()), attributeName = attr
                .getName(), key = annFilePathMapKey(datasetConfigName,
                attributeName), path = annotationsFilePaths.get(key);

        if (path == null) {
            // 1.1 get annotations and put them on disk
            File annotationFile;

            try {

                annotationFile = File.createTempFile(key, "annotations");
                annotationFile.deleteOnExit();

            } catch (IOException e) {
                Log.error(this.getClass().getName()
                        + "#getAnnotationsFilePath(" + attributeList
                        + ") impossible to create a temporary file", e);
                return "";
            }

            try (FileOutputStream oStream = new FileOutputStream(annotationFile)) {

                path = annotationFile.getPath();
                queryForAnnotations(attributeList, oStream);
                annotationsFilePaths.put(key, path);

            } catch (IOException e) {
                Log.error(this.getClass().getName()
                        + "#getAnnotationsFilePath(" + attributeList
                        + ") impossible to write on temporary file.", e);
                return "";
            }
        }

        return path;
    }

    /**
     * Provides the key for the annotations to file path map.
     * 
     * @param datasetConfigName
     *            Name of the dataset config the attribute list belongs to.
     * @param attributeName
     *            The name of the second attribute within the attribute list
     *            (See documentation).
     * @return Key for the annotations to file path map.
     */
    private String annFilePathMapKey(String datasetConfigName,
            String attributeName) {
        return datasetConfigName + attributeName;
    }

    /**
     * Submits a query for gene id translation.
     * 
     * @param function
     *            The function of the filter of which value must be translated.
     * @param attributeList
     *            Attribute list that includes info mandatory for translation.
     * @param filterName
     * @param filterValue
     *            Value to translate.
     * @param o
     */
    public void toEnsemblGeneId(String attributeList, String filterName,
            String filterValue, OutputStream o) {
        Element tmpe = null;
        Attribute forTransAttr = null;
        QueryElement qelem = null;
        // We can get the queryelement from the function of element it wraps.
        Map<String, QueryElement> bindings = this.metadata.getBindings();

        qelem = bindings.get(attributeList);

        tmpe = Utils.getAttributeForEnsemblGeneIdTranslation(qelem);

        // It means it didn't find a filter list or qelem doesn't wrap an
        // attribute list.
        if (tmpe == null) {
            Log.error(this.getClass().getName()
                    + "#toEnsemblGeneId(): "
                    + "cannot get the necesary attribute needed for translation. "
                    + "Maybe " + attributeList + " is not an attribute list?");
            return;
        }

        forTransAttr = (Attribute) tmpe;

        submitToEnsemblIdQuery(qelem, forTransAttr, filterName, filterValue, o);
    }

    private void submitToEnsemblIdQuery(QueryElement qeAttr,
            Attribute transAttr, String filterName, String filterValue,
            OutputStream o) {
        initQueryBuilder();
        qbuilder.setDataset(qeAttr.getDataset().getName(),
                qeAttr.getConfig().getName()).addAttribute(transAttr.getName())
                .addFilter(filterName, filterValue).getResults(o);
    }

    /**
     * Retrieves annotations based on the input attribute from the query.
     * 
     * @param attributeList
     * @param o
     */
    public void queryForAnnotations(String attributeList, OutputStream o) {
        Element annRetr = null, geneRetr;
        QueryElement qelem = null;

        // We can get the queryelement from the function of element it wraps.
        Map<String, QueryElement> bindings = this.metadata.getBindings();

        qelem = bindings.get(attributeList);

        // This is for the annotation column
        annRetr = Utils.getAttributeForAnnotationRetrieval(qelem);

        // It means it didn't find a filter list or qelem doesn't wrap an
        // attribute list.
        if (annRetr == null) {
            Log.error(this.getClass().getName()
                    + "#toEnsemblGeneId(): "
                    + "cannot get the necesary attribute needed for translation. "
                    + "Maybe " + attributeList + " is not an attribute list?");
            return;
        }

        // This is for the gene column
        geneRetr = Utils.getAttributeForEnsemblGeneIdTranslation(qelem);

        submitAnnotationsQuery((Attribute) annRetr, (Attribute) geneRetr, o);
    }

    private void submitAnnotationsQuery(Attribute st, Attribute sd,
            OutputStream o) {
        initQueryBuilder();
        qbuilder.setDataset(Utils.getDatasetName(st),
                Utils.getDatasetConfig(st)).addAttribute(st.getName())
                .addAttribute(sd.getName()).getResults(o);
    }

    private void initQueryBuilder() {
        qbuilder.init();
    }

    public ByteArrayOutputStream byteStream() {
        return new ByteArrayOutputStream();
    }

    public String getBackground() {
        return background;
    }

    public String getSets() {
        return sets;
    }

    public String getAnnotation() {
        return annotation;
    }

    public String getClient() {
        return client;
    }

    public String getCutoff() {
        return cutoff;
    }

    public String toString() {
        return "EnrichmentDino(background = " + background + "  " + "sets = "
                + sets + "  " + "annotation = " + annotation + "  "
                + "cutoff = " + cutoff + "  " + "client = " + client + ")";
    }

    private String toString(ByteArrayOutputStream o)
            throws UnsupportedEncodingException {
        return o.toString("UTF-8");
    }

    @Override
    public Dino setQuery(org.biomart.queryEngine.Query query) {
        q = query;
        return this;
    }

    @Override
    public Dino setMimes(String[] mimes) {
        return this;
    }

    @Override
    public Dino setMetaData(MetaData md) {
        this.metadata = md;
        return this;
    }

    public MetaData getMetaData() {
        return this.metadata;
    }

    public QueryBuilder getQueryBuilder() {
        return this.qbuilder;
    }

}
