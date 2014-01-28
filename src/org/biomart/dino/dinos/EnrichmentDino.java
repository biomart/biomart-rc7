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
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.biomart.common.exceptions.TechnicalException;
import org.biomart.common.resources.Log;
import org.biomart.dino.Binding;
import org.biomart.dino.Utils;
import org.biomart.dino.annotations.Func;
import org.biomart.dino.command.HypgCommand;
import org.biomart.dino.command.HypgRunner;
import org.biomart.dino.querybuilder.QueryBuilder;
import org.biomart.objects.objects.Attribute;
import org.biomart.objects.objects.Dataset;
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

    // NOTE: these will contain filter values and attribute names.
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
    Binding metadata;

    // Temporary files.
    File backgroundInput, setsInput;
    
    OutputStream sink;

    @Inject
    public EnrichmentDino(HypgCommand cmd, HypgRunner cmdRunner,
            @Named("JavaApi") QueryBuilder qbuilder) {
        this.cmd = cmd;
        this.cmdRunner = cmdRunner;
        this.qbuilder = qbuilder;
    }

    @Override
    public void run(OutputStream out) throws TechnicalException {
        Log.debug(this.getClass().getName() + "#run(OutputStream) invoked");
        
        sink = out;
        
        iterate();
//        translateFilters();
//        String annFile = getAnnotationsFilePath(annotation);
//        
//        try {
//            if (annFile.isEmpty()) {
//                throw new IOException("Cannot find annotations file nor retrieve them");
//            }
//            
//            if (q.getClient().equalsIgnoreCase("false")) {
//                // serve plain results
//                // The bin path is sat within the DinoModule as a constant.
//                cmd.setAnnotations(new File(annFile))
//                    .setBackground(backgroundInput)
//                    .setSets(setsInput)
//                    .setCutoff(cutoff);
//    
//                @SuppressWarnings("unchecked")
//                List<String[]> results = (List<String[]>) cmdRunner.setCmd(cmd)
//                        .run()
//                        .getResults();
//            } else {
//                // serve app
//            }
//            
//        } catch (IOException e) {
//            throw e;
//        } catch (InterruptedException e) {
//            Log.error(this.getClass().getName() + "enrichment interrupted ", e);
//            throw new IOException(e);
//        } finally {
//            backgroundInput.delete();
//            setsInput.delete();
//        }
    }

    /**
     * 
     * For each Attribute List we:
     * + create a binding on fields of this class using this attribute list and then filters and filter lists.
     * + translate filter values
     * + get annotations
     * + run enrichment and get results 
     * @throws TechnicalException 
     * 
     */
    private void iterate() throws TechnicalException {
        for (QueryElement attrList : q.getAttributeListList()) {
            iteration(attrList);
        }
    }
    
    private void iteration(QueryElement queryAttrList) throws TechnicalException {
        
        this.metadata.clear();
        
        List<Field> myFields = Binding.getAnnotatedFields(this.getClass());
        
//        Attribute attrList = (Attribute)queryAttrList.getElement();
        // Attributes inside the Attribute List
//        List<Attribute> attrs = attrList.getAttributeList();
        List<QueryElement> attrs = new ArrayList<QueryElement>();
        attrs.add(queryAttrList);
        
        List<QueryElement> filters = this.q.getFilters();
        
        List<QueryElement> attrsAsQuery, boundAttrs, boundFilts;
        
        try {
            // This is necessary since the only way to get hold of filter
            // values is from a QueryElement and we have a unique method for
            // Attributes and Filters.
            // The translation isn't even expensive since there are usually 
            // few Attributes.
//            attrsAsQuery = fromAttributeToQueryElementList(queryAttrList, attrs);
            boundAttrs = Binding.setFieldValues(this, myFields, attrs);
            boundFilts = Binding.setFieldValues(this, myFields, filters);

            this.metadata.setBindings(myFields, boundAttrs);
            this.metadata.setBindings(myFields, boundFilts);
            
            // It throws a ValidationException if any field wasn't bound.
            this.metadata.checkBinding(myFields);
            
            tasks();
        } catch (IOException | IllegalArgumentException | IllegalAccessException e) {
            throw new TechnicalException(e);
        }
    }
    
    private void tasks() throws IOException {
        translateFilters();
        enrich();
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
    private void sendResults(InputStream i, OutputStream o) throws IOException {
        // use stream pipeline
    }
    
    private void enrich() throws IOException {
        String annPath = this.getAnnotationsFilePath(ANNOTATION);
        
        try {
            if (annPath.isEmpty()) {
                throw new IOException("Cannot find annotations file nor retrieve them");
            }

            // The bin path is sat within the DinoModule as a constant.
            cmd.setAnnotations(new File(annPath))
                .setBackground(backgroundInput)
                .setSets(setsInput)
                .setCutoff(cutoff);

            @SuppressWarnings("unchecked")
            List<String[]> results = (List<String[]>) cmdRunner.setCmd(cmd)
                                                                .run()
                                                                .getResults();
            // If gui client
            sendResults(results, sink);
            // else 

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
    
    private void translateFilters() throws IOException {
        Log.debug(this.getClass().getName() + "#translatesFilters()");
        
        try {
            
            backgroundInput = File.createTempFile("input", "filter");
            translateSingleFilter(BACKGROUND, background, backgroundInput);
            setsInput = File.createTempFile("input", "filter");
            translateSingleFilter(SETS, sets, setsInput);
            
        } catch (IOException e) {
            Log.error(this.getClass().getName() + "#translateFilters() "
                    + "impossible to write on temporary file or the file is missing .", e);
            
            if (backgroundInput.exists()) 
                backgroundInput.delete();
            if (setsInput.exists()) 
                setsInput.delete();
            
            
            throw e;
        }

    }
    
    private void translateSingleFilter(String filter, String filterValue, File outFile) throws IOException {
        Map<String, Element> bind = this.metadata.getBindings();
        Element e = null;

        Log.debug(bind);
        
        try (FileOutputStream oStream = new FileOutputStream(outFile)) {
            Log.debug("EnrichmentDino::translateFilters() for filter : "+ filter);
    
            e = bind.get(filter);
            String filterName = e.getName();
            
            toEnsemblGeneId(ANNOTATION, filterName, filterValue, oStream);
        } 
    }
    
    /**
     * Submits a query for gene id translation.
     * 
     * @param attributeList Attribute name that includes info mandatory for translation.
     * @param filterName Name of the filter to translate.
     * @param filterValue Value to translate.
     * @param o Stream through which send results.
     */
    private void toEnsemblGeneId(String attributeList, 
                                String filterName,
                                String filterValue, 
                                OutputStream o) {
        
        Attribute forTransAttr = null, elem = null;
        
        Map<String, Element> bindings = this.metadata.getBindings();

        // Get the attribute object.
        elem = (Attribute)bindings.get(attributeList);
        
        Log.debug(this.getClass().getName() + "#toEnsemblGeneId()" 
                + " attributeList = "+ attributeList 
                + " element got = "+ elem);
        
        forTransAttr = getAttributeForIdTranslation(elem);

        // It means it didn't find any filter list or elem isn't an attribute 
        // list.
        if (forTransAttr == null) {
            Log.error(this.getClass().getName()
                    + "#toEnsemblGeneId(): "
                    + "cannot get the necessary attribute needed for translation. "
                    + "Maybe " + attributeList + " is not an attribute list?");
            return;
        }

        submitToEnsemblIdQuery(elem, forTransAttr, filterName, filterValue, o);
    }
    
    private Attribute getAttributeForIdTranslation(Attribute attr) {
        return Utils.getAttributeForEnsemblGeneIdTranslation(attr);
    }

    private void submitToEnsemblIdQuery(Attribute attr,
                                        Attribute transAttr, 
                                        String filterName, 
                                        String filterValue,
                                        OutputStream o) {
        
        initQueryBuilder();
        qbuilder.setDataset(getDatasetName(attr), getConfigName(attr))
                .addAttribute(transAttr.getName())
                .addFilter(filterName, filterValue)
                .getResults(o);
    }
    
    private String getDatasetName(Attribute attr) {
        return this.q.getQueryElementList()
                .get(0)
                .getDataset()
                .getName();
    }
    
    private String getConfigName(Attribute attr) {
        return this.q.getQueryElementList()
                .get(0)
                .getConfig()
                .getName();
    }

    /**
     * It returns the absolute path to the annotation file if already present.
     * Otherwise, it gathers and put them on disk as temporary file.
     * 
     * @param attributeList
     * @return a path or empty list if something went wrong.
     */
    private String getAnnotationsFilePath(String attributeList) {
        // 1. check if already present on disk
        Attribute attrListElem = (Attribute) this.metadata.getBindings().get(attributeList);
        String datasetName = "", configName = "";
        
        if (isSpecieTranslation(attrListElem)) {
            datasetName = getDatasetNameForSpecieTranslation(attrListElem);
            configName = getConfigNameForSpecieTranslation(attrListElem);
        } else {
            QueryElement qe = this.q.getQueryElementList().get(0);
            datasetName = qe.getDataset()
                            .getName();
            configName = qe.getConfig()
                           .getName();
        }
        
        Attribute a2 = Utils.getAttributeForAnnotationRetrieval(attrListElem);
        String key = annFilePathMapKey(configName, a2.getName()),
               path = annotationsFilePaths.get(key);
        
        if (path == null) {
            // 1.1 get annotations and put them on disk
            File annotationFile;

            try {

                annotationFile = File.createTempFile(key, "annotations");
                annotationFile.deleteOnExit();

            } catch (IOException ex) {
                Log.error(this.getClass().getName()
                        + "#getAnnotationsFilePath(" + attributeList
                        + ") impossible to create a temporary file", ex);
                return "";
            }

            try (FileOutputStream oStream = new FileOutputStream(annotationFile)) {

                path = annotationFile.getPath();
                // TODO: check file content.
                submitAnnotationsQuery(datasetName, 
                                       configName, 
                                       a2.getName(), 
                                       oStream);
                
                annotationsFilePaths.put(key, path);

            } catch (IOException ex) {
                Log.error(this.getClass().getName()
                        + "#getAnnotationsFilePath(" + attributeList
                        + ") impossible to write on temporary file.", ex);
                return "";
            }
        }

        return path;
    }
    
    private boolean isSpecieTranslation(Attribute attrListElem) {
        Attribute a1 = Utils.getAttributeForEnsemblGeneIdTranslation(attrListElem);
        return a1.getName().contains("homology");
    }
    
    private String getDatasetNameForSpecieTranslation(Attribute attrListElem) {
        return Utils.getDatasetName(attrListElem);
    }
    
    private String getConfigNameForSpecieTranslation(Attribute attrListElem) {
        return Utils.getConfigName(attrListElem);
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
     * Retrieves annotations based on the input attribute from the query.
     * 
     * @param attributeList
     * @param o
     */
//    public void queryForAnnotations(String attributeList, OutputStream o) {
//        Element annRetr = null, geneRetr;
//        Attribute elem = null;
//
//        // We can get the queryelement from the function of element it wraps.
//        Map<String, Element> bindings = this.metadata.getBindings();
//
//        elem = (Attribute) bindings.get(attributeList);
//
//        // This is for the annotation column
//        annRetr = Utils.getAttributeForAnnotationRetrieval(elem);
//
//        // It means it didn't find a filter list or qelem doesn't wrap an
//        // attribute list.
//        if (annRetr == null) {
//            Log.error(this.getClass().getName()
//                    + "#toEnsemblGeneId(): "
//                    + "cannot get the necesary attribute needed for translation. "
//                    + "Maybe " + attributeList + " is not an attribute list?");
//            return;
//        }
//
//        // This is for the gene column
//        geneRetr = Utils.getAttributeForEnsemblGeneIdTranslation(elem);
//
//        submitAnnotationsQuery((Attribute) annRetr, (Attribute) geneRetr, o);
//    }

    private void submitAnnotationsQuery(String dataset, 
                                        String config,
                                        String attribute,
                                        OutputStream o) {
        initQueryBuilder();
        qbuilder.setDataset(dataset, config)
                .addAttribute(attribute)
                .addAttribute("ensembl_gene_id")
                .getResults(o);
    }

    private void initQueryBuilder() {
        qbuilder.init()
            .setUseDino(false);
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

    private String getAnnotation() {
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
    public Dino setMetaData(Binding md) {
        this.metadata = md;
        return this;
    }

    public Binding getMetaData() {
        return this.metadata;
    }

    public QueryBuilder getQueryBuilder() {
        return this.qbuilder;
    }
    
    private List<QueryElement> 
    fromAttributeToQueryElementList(QueryElement source, List<Attribute> attrs) {
        List<QueryElement> qs = new ArrayList<QueryElement>(attrs.size());
        Dataset dataset = source.getDataset();
        
        for (Attribute e : attrs) 
            qs.add(new QueryElement(e, dataset));
        
        return qs;
    }

}









































