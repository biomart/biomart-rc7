package org.biomart.dino.dinos.enrichment;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.biomart.common.exceptions.TechnicalException;
import org.biomart.common.resources.Log;
import org.biomart.dino.Binding;
import org.biomart.dino.Utils;
import org.biomart.dino.annotations.Func;
import org.biomart.dino.command.HypgCommand;
import org.biomart.dino.command.HypgRunner;
import org.biomart.dino.dinos.Dino;
import org.biomart.dino.querybuilder.QueryBuilder;
import org.biomart.objects.objects.Attribute;
import org.biomart.objects.objects.Element;
import org.biomart.queryEngine.Query;
import org.biomart.queryEngine.QueryElement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * NOTE This implementation assumes the incoming query has only one attribute!
 *
 * @author luca
 *
 */
public class EnrichmentDino implements Dino {
    static public final String BACKGROUND = "background",
                               SETS = "sets",
                               ANNOTATION = "annotation",
                               CUTOFF = "cutoff";

    // Key: dataset_config_name + attribute_list name
    // Value: path of the annotation file
    static private Map<String, String> annotationsFilePaths =
                                                new HashMap<String, String>();

    static ObjectMapper mapper = new ObjectMapper();

    static final List<String> resultsSeparator =
                                    new ArrayList<String>(1) {{ add("---"); }};


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

    // These are dataset and configuration used for annotation retrieval
    // at the time of this request.
    String annotationDatasetName = "", annotationConfigName = "";

    Map<String, List<List<String>>> results = new HashMap<String, List<List<String>>>();
    // These are collections to use when the request comes from a web browser
//    List<Map<String, String>> nodes = new ArrayList<Map<String, String>>();

//    Map<String, List<String>> annotationKeys = new HashMap<String, List<String>>();
//    Map<String, List<String>> geneKeys = new HashMap<String, List<String>>();

//    Map<String, List<List<String>>> annotations = new HashMap<String, List<List<String>>>();
//    Map<String, List<List<String>>> genes = new HashMap<String, List<List<String>>>();

    // Nodes are shared amongst attribute lists
    List<Map<String, Object>> nodes = new ArrayList<Map<String, Object>>();

    // Links are segregated per attribute list
    Map<String, List<Map<String, Object>>> links = new HashMap<String, List<Map<String, Object>>>();
    String id = "_id", linkSource = "source", linkTarget = "target";

    OutputStream sink;

    Map<String, Object> config;
    GuiResponseCompiler compiler;

    @Inject
    public EnrichmentDino(HypgCommand cmd,
                          HypgRunner cmdRunner,
                          @Named("JavaApi")
                          QueryBuilder qbuilder,
                          @Named("Enrichment File Config Path")
                          String configPath,
                          GuiResponseCompiler compiler) throws IOException {
        this.cmd = cmd;
        this.cmdRunner = cmdRunner;
        this.qbuilder = qbuilder;

        config = mapper.readValue(new File(configPath), Map.class);
        if (config == null) {
            config = new HashMap<String, Object>();
        }
        
        this.compiler = compiler;
    }

    @Override
    public void run(OutputStream out) throws TechnicalException, IOException {
        Log.debug(this.getClass().getName() + "#run(OutputStream) invoked");

        sink = out;

        iterate();
    }

    /**
     *
     * For each Attribute List we:
     * + create a binding on fields of this class using this attribute list and then filters and filter lists.
     * + translate filter values
     * + get annotations
     * + run enrichment and get results
     * @throws TechnicalException
     * @throws IOException
     *
     */
    private void iterate() throws TechnicalException, IOException {
        for (QueryElement attrList : q.getAttributeListList()) {
            iteration(attrList);
        }
        
        if (this.isGuiClient()) {
            results = null;
            sendGuiResponse();
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

        List<QueryElement> boundAttrs, boundFilts;

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
        if (isGuiClient()) {
            handleGuiRequest();
        } else {
            handleWebServiceRequest();
         // Separator between results of different attribute lists
            sink.write("\n\n\n".getBytes());
        }
    }


    private void handleWebServiceRequest() {
        String processor = this.q.getProcessor();
        List<List<String>> res = results.get(annotation);
        // This modifies res as we want
        this.webServiceToAnnotationHgncSymbol(res);
        List<String[]> ares = new ArrayList<String[]>(res.size());

        if (this.q.hasHeader())
            ares.add(new String[] { "Annotation", "Description", "P-Value", "Bonferroni p-value", "Genes" });

        for (List<String> r : res) {
            ares.add(r.toArray(new String[r.size()]));
        }

        results.remove(res);
        res = null;
        results.remove(annotation);

        try {
            org.biomart.dino.Processor.runProcessor(ares, processor, q, sink);
        } catch (IllegalArgumentException | InstantiationException
                | IllegalAccessException | InvocationTargetException e) {
            Log.error("EnrichmentDino#handleWebServiceRequest(): cannot send results: ",e);
        }
    }


    private void handleGuiRequest() {
        List<List<String>> data = results.get(annotation);

        // Truncate results
        if (data.size() > 50) {
            data = data.subList(0, 50);
        }

        // Translate ensembl ids into hgnc symbols and gather further attributes
        // specified within the configuration.
        this.guiToAnnotationHgncSymbol(data);
    }
    
    
    private void sendGuiResponse() throws IOException {
        try(ByteArrayOutputStream out = byteStream()) {
            String p = config.get("front-end").toString();
            mkJson(out);
            Map<String, Object> scope = new HashMap<String, Object>();
            scope.put("data", out.toString());
            GuiResponseCompiler.compile(new File(p), sink, scope);
        }
    }
    

    private void mkJson(OutputStream out) {
        com.fasterxml.jackson.databind.ObjectMapper m = new com.fasterxml.jackson.databind.ObjectMapper();
        
        Map<String, Object> root = new HashMap<String, Object>(),
                tabs = new HashMap<String, Object>(), edges;
        
        root.put("nodes", nodes);
        
        for (String ann : links.keySet()) {
            edges = new HashMap<String, Object>();
            tabs.put("links", links.get(ann));
            tabs.put(ann, edges);
        }
        
        root.put("tabs", tabs);
        
        try {
            m.writeValue(out, root);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
//    private void translateResults(List<List<String>> data) throws IOException {
//        ByteArrayOutputStream tmpStream;
//
//        int sourceIndex = -1, targetIndex = -1;
//        ICsvMapReader reader = null;
//        List<String> header = null;
//
//        for (List<String> row : data) {
//
//            if (row.size() > 0) {
//                tmpStream = byteStream();
//                // It has the header too.
//                toAnnotationHgncSymbol(row.get(0), tmpStream);
//                reader = getTranslatedResults(tmpStream);
//                // It's only one line.
//                targetIndex = addNodes(tr);
//                tmpStream.close();
//            } else {
//                Log.error(this.getClass().getName() + "#translateResults(): "
//                        + "skipping bad-formatted row");
//            }
//
//            // If there are genes involved with this annotation.
//            // Genes should be comma separated
//            if (! row.get(3).isEmpty()) {
//                tmpStream = byteStream();
//                toGeneHgncSymbol(row.get(3), tmpStream);
//                tr = getTranslatedResults(tmpStream);
//                sourceIndex = addNodes(tr);
//            }
//
//            if (sourceIndex != -1 && targetIndex != -1) {
//                for (int i = 0, len = tr.size(); i < len; ++i) {
//                    edges.add(new int[]{ sourceIndex + i, targetIndex });
//                }
//            }
//        }
//    }

    private List<String> getResultsHeader(ByteArrayOutputStream oStream) {
        java.util.StringTokenizer lineSt =
                new java.util.StringTokenizer(oStream.toString(), "\\n");
        if (lineSt.hasMoreTokens()) {
            return tokenizeLine(lineSt.nextToken());
        } else {
            return new ArrayList<String>();
        }
    }

    // TODO: use a piped stream with threads
//    private ICsvMapReader getTranslatedResults(ByteArrayOutputStream oStream) throws IOException {
//
//        java.io.StringReader sr = new java.io.StringReader(oStream.toString());
//        ICsvMapReader mapReader = new CsvMapReader(sr, CsvPreference.TAB_PREFERENCE);
//        this.headers.put(annotation, mapReader.getHeader(true));
//
//        return mapReader;
//    }

    private List<String> tokenizeLine(String line) {
        java.util.StringTokenizer colSt =
                new java.util.StringTokenizer(line);
        List<String> list = new ArrayList<String>(colSt.countTokens());

        while(colSt.hasMoreElements()) { list.add(colSt.nextToken()); }

        return list;
    }


//    // TODO: print the header if requested.
//    private void sendResults(InputStream i, OutputStream o) throws IOException {
//
//    }

    @SuppressWarnings("unchecked")
    private void enrich() throws IOException {
        String annPath = this.getAnnotationsFilePath(ANNOTATION);
        File bin = new File(System.getProperty("biomart.basedir"),
                            getEnrichmentBinPath(config));
        try {
            if (annPath.isEmpty()) {
                throw new IOException("Cannot find annotations file nor retrieve them");
            }

            // The bin path is sat within the DinoModule as a constant.
            cmd.setAnnotations(new File(annPath))
                .setBackground(backgroundInput)
                .setSets(setsInput)
                .setCutoff(cutoff)
                .setCmdBinPath(bin);

            List<List<String>> newResult =
                    (List<List<String>>) cmdRunner.setCmd(cmd).run().getResults();

            results.put(annotation, newResult);

        } catch (IOException e) {
            throw e;
        } catch (InterruptedException e) {
            Log.error(this.getClass().getName() + "enrichment interrupted ", e);
            throw new IOException(e);
        } finally {
            if (backgroundInput != null) backgroundInput.delete();
            if (setsInput != null) setsInput.delete();
        }

    }

    private void translateFilters() throws IOException {
        Log.debug(this.getClass().getName() + "#translatesFilters()");

        try {

            backgroundInput = File.createTempFile("background", "filter");
            translateBackgroundFilter(BACKGROUND, background, backgroundInput);
            setsInput = File.createTempFile("sets", "filter");
            translateSetsFilter(SETS, sets, setsInput);

        } catch (IOException e) {
            Log.error(this.getClass().getName() + "#translateFilters() "
                    + "impossible to write on temporary file or the file is missing .", e);

            if (backgroundInput.exists()) backgroundInput.delete();
            if (setsInput.exists()) setsInput.delete();

            throw e;
        }

    }

    private void translateSetsFilter(String filter, String filterValue, File outFile) throws IOException {
        Log.debug("sets value: "+ filterValue);

        try (FileOutputStream oStream = new FileOutputStream(outFile)) {
            String setName = "set";
            oStream.write((">" + setName + "\n").getBytes());
            translateSingleFilter(filter, filterValue, oStream);
            oStream.write(("<" + setName + "\n").getBytes());
        }
    }

    private void translateBackgroundFilter(String filter, String filterValue, File outFile) throws IOException {
        try (FileOutputStream oStream = new FileOutputStream(outFile)) {
            translateSingleFilter(filter, filterValue, oStream);
        }
    }

    private void translateSingleFilter(String filter, String filterValue, FileOutputStream out) {
        Map<String, Element> bind = this.metadata.getBindings();
        Element e = null;

        Log.debug(bind);

        Log.debug("EnrichmentDino::translateFilters() for filter : "+ filter);

        e = bind.get(filter);
        String filterName = e.getName();

        toEnsemblGeneId(ANNOTATION, filterName, filterValue, out);
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

        submitToEnsemblIdQuery(forTransAttr, filterName, filterValue, o);
    }

    @SuppressWarnings("unchecked")
    private List<String> getDisplayAttributes(Map<String, Object> cfg) {
        Object o = cfg.get("attributes");
        List<Object> atts = n((List<Object>) o);
        List<String> attNames = new ArrayList<String>(atts.size());
        for (Object oo : atts) attNames.add(((String) oo));

        return attNames;
    }

    private String getDisplayFilter(Map<String, Object> cfg) {
        Object o = cfg.get("filter");
        return o == null ? "" : o.toString();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getDisplayGeneOptions(Map<String, Object> cfg) {
        Object o = cfg.get("gene");
        return n((Map<String, Object>) o);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getDisplayAnnotationOptions(Map<String, Object> cfg) {
        Object o = cfg.get("annotation");
        return n((Map<String, Object>) o);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getDisplayOptions() {
        Object o = config.get("display");
        return n((Map<String, Object>) o);
    }

    private Map<String, Object> n(Map<String, Object> m) {
        return m == null ? new HashMap<String, Object>() : m;
    }

    private List<Object> n(List<Object> l) {
        return l == null ? new ArrayList<Object>() : l;
    }

    private String getEnrichmentBinPath(Map<String, Object> cfg) {
        String s = (String) cfg.get("enrichment_bin");
        return s == null ? "" : s;
    }


    /**
     * If the current request is coming from GUI, it sends a query for hgnc
     * symbol translation with filterValue as value of the filter, plus all
     * attributes specified within the display section of the configuration
     * of this Dino.
     *
     * Otherwise, it just translates filterValue considering only the filter
     * and the *first* attribute specified within the configuration.
     *
     * @param filterValue
     * @param out
     */
//    private void toGeneHgncSymbol(String filterValue, OutputStream out) {
//        Map<String, Object> opt = getDisplayOptions(),
//                gene = (Map<String, Object>) getDisplayGeneOptions(opt);
//
//        List<String> atts = getDisplayAttributes(gene);
//        String fName = getDisplayFilter(gene),
//               ds = getDatasetName(),
//               cfg = getConfigName();
//
//        submitToHgncSymbolQuery(ds,
//                                cfg,
//                                fName,
//                                filterValue,
//                                isGuiClient() ? atts : atts.subList(0, 1),
//                                out);
//
//    }

    /**
     * Same as toGeneHgncSymbol.
     *
     * @param filterValue
     * @param out
     */
//    private void toAnnotationHgncSymbol(String filterValue, OutputStream out) {
//
//        Log.debug("toAnnotationHgncSymbol "+ annotation);
//
//
//        @SuppressWarnings("unchecked")
//        Map<String, Object> opt = getDisplayOptions(),
//                ann = (Map<String, Object>) getDisplayAnnotationOptions(opt).get(annotation);
//
//        if (ann == null) {
//            ann = new HashMap<String, Object>();
//        }
//
//        List<String> atts = getDisplayAttributes(ann);
//        String fName = getDisplayFilter(ann);
//
//        submitToHgncSymbolQuery(annotationDatasetName,
//                                annotationConfigName,
//                                fName,
//                                filterValue,
//                                isGuiClient() ? atts : atts.subList(0, 1),
//                                out);
//
//    }
    private Map<String, Object> mkNode(List<String> ks, List<String> vs) {
        Map<String, Object> n = new HashMap<String, Object>(ks.size());
        for (int i = 0, len = ks.size(); i < len; ++i) {
            n.put(ks.get(i), vs.get(i));
        }
        return n;
    }

    private Map<String, Object> mkLink(List<String> ks, List<Integer> vs) {
        Map<String, Object> n = new HashMap<String, Object>(ks.size());
        for (int i = 0, len = ks.size(); i < len; ++i) {
            n.put(ks.get(i), vs.get(i));
        }
        return n;
    }

    private void guiToAnnotationHgncSymbol(List<List<String>> data) {

        Log.debug("guiToAnnotationHgncSymbol "+ annotation);

        @SuppressWarnings("unchecked")
        Map<String, Object> opt = getDisplayOptions(),
                ann = (Map<String, Object>) getDisplayAnnotationOptions(opt).get(annotation),
                gene = (Map<String, Object>) getDisplayGeneOptions(opt);

        if (ann == null || gene == null) {
            return;
        }

        String lineDelim = "\n", colDelim = "\t";
        List<String> annAtts = new ArrayList<String>(),
                     geneAtts = new ArrayList<String>();

        annAtts.add(ann.get("annotation_attribute").toString());
        annAtts.add(ann.get("description_attribute").toString());
        annAtts.addAll((List<String>)ann.get("other_attributes"));

        String annFilterName = getDisplayFilter(ann);

        geneAtts.add(gene.get("gene_attribute").toString());
        geneAtts.add(gene.get("description_attribute").toString());
        geneAtts.addAll((List<String>)gene.get("other_attributes"));
        String geneFilterName = getDisplayFilter(gene);

        String[] lines;

        List<String> cols, aKeys = null, gKeys = null, linkKeys = Arrays.asList(linkSource, linkTarget);
        String[] colsArray;

        boolean wantHeader = true;
        
        links.put(annotation, new ArrayList<Map<String, Object>>());

        try(ByteArrayOutputStream out = byteStream()) {

            for (List<String> line : data) {
                out.reset();
                int annotationTargetIdx = -1, contentLineIdx = 0;

                if (wantHeader) contentLineIdx = 1;

                submitToHgncSymbolQuery(annotationDatasetName, annotationConfigName,
                        annFilterName, line.get(0), annAtts, wantHeader, out);

                // There are two lines: header and the converted annotation
                // each with k columns.
                lines = out.toString().split(lineDelim);

                // Keys
                if (wantHeader) {
                    colsArray = lines[0].split(colDelim);
                    aKeys = new ArrayList<String>(Arrays.asList(id, "p-value", "bp-value"));
                    aKeys.addAll(Arrays.asList(Arrays.copyOfRange(colsArray, 1, colsArray.length)));
                    wantHeader = false;
                }

                // The actual content
                colsArray = lines[contentLineIdx].split(colDelim);

                // Add annotation, p-value, bp-value
                cols = new ArrayList<String>(Arrays.asList(colsArray[0], line.get(1), line.get(2)));
                // Add the rest of the columns
                cols.addAll(Arrays.asList(Arrays.copyOfRange(colsArray, 1, colsArray.length)));

                for (int a = 0, alen = nodes.size(); a < alen; ++a) {
                    Map<String, Object> m = nodes.get(a);
                    if (m.get(id).equals(colsArray[0])) {
                        annotationTargetIdx = a;
                        break;
                    }
                }

                if (annotationTargetIdx == -1) {
                    annotationTargetIdx = nodes.size();
                    nodes.add(mkNode(aKeys, cols));
                }

                lines = null; colsArray = null; cols = null;

                if (line.size() > 3) {
                    out.reset();

                    submitToHgncSymbolQuery(annotationDatasetName, annotationConfigName,
                            geneFilterName, line.get(3), geneAtts, true, out);

                    lines = out.toString().split(lineDelim);
                    colsArray = lines[0].split(colDelim);
                    gKeys = new ArrayList<String>(Arrays.asList(id));
                    gKeys.addAll(Arrays.asList(Arrays.copyOfRange(colsArray, 1, colsArray.length)));

                    colsArray = null; cols = null;


                    for (int i = 1, len = lines.length; i < len; ++i) {
                        int geneSourceIdx = -1;
                        String geneLine = lines[i];
                        colsArray = geneLine.split(colDelim);
                        cols = new ArrayList<String>(Arrays.asList(colsArray));
                        for (int j = 0, jlen = nodes.size(); j < jlen; ++j) {
                            Map<String, Object> m = nodes.get(j);
                            if (m.get(id).equals(colsArray[0])) {
                                geneSourceIdx = j;
                                break;
                            }
                        }

                        if (geneSourceIdx == -1) {
                            geneSourceIdx = nodes.size();
                            nodes.add(mkNode(gKeys, cols));
                        }

                        links.get(annotation).add(mkLink(linkKeys, Arrays.asList(geneSourceIdx, annotationTargetIdx)));
                    }
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private void webServiceToAnnotationHgncSymbol(List<List<String>> data) {

        Log.debug("webServiceToAnnotationHgncSymbol "+ annotation);

        @SuppressWarnings("unchecked")
        Map<String, Object> opt = getDisplayOptions(),
                ann = (Map<String, Object>) getDisplayAnnotationOptions(opt).get(annotation),
                gene = (Map<String, Object>) getDisplayGeneOptions(opt);

        if (ann == null || gene == null) {
            return;
        }

        String delim = "[\t\n]";
        List<String> annAtts = new ArrayList<String>(),
                     geneAtts = new ArrayList<String>();

        annAtts.add(ann.get("annotation_attribute").toString());
        annAtts.add(ann.get("description_attribute").toString());
        String annFilterName = getDisplayFilter(ann);
        geneAtts.add(gene.get("gene_attribute").toString());
        String geneFilterName = getDisplayFilter(gene);
        String[] atmp;

        try(ByteArrayOutputStream out = byteStream()) {

            for (List<String> line : data) {
                out.reset();

                submitToHgncSymbolQuery(
                        annotationDatasetName,
                        annotationConfigName,
                        annFilterName, line.get(0),
                        annAtts, false,
                        out);

                atmp = out.toString().split(delim);

                if (atmp.length > 1) {
                    line.set(0, atmp[0]);
                    line.add(1, atmp[1]);
                }

                if (line.size() > 4) {
                    out.reset();

                    submitToHgncSymbolQuery(
                            annotationDatasetName,
                            annotationConfigName,
                            geneFilterName, line.get(4),
                            geneAtts, false,
                            out);

                    atmp = out.toString().split(delim);
                    line.set(4, StringUtils.join(atmp, ","));
                    atmp = null;
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    private void submitToHgncSymbolQuery(
                              String datasetName,
                              String configName,
                              String filterName,
                              String filterValue,
                              List<String> attributes,
                              boolean header,
                              OutputStream out) {
        initQueryBuilder();
        qbuilder.setHeader(header)
                .setDataset(datasetName, configName)
                .addFilter(filterName, filterValue);
        for (String att : attributes) {
            qbuilder.addAttribute(att);
        }

        qbuilder.getResults(out);
    }

    private Attribute getAttributeForIdTranslation(Attribute attr) {
        return Utils.getAttributeForEnsemblGeneIdTranslation(attr);
    }

    private String getDatasetName() {
        return this.q.getQueryElementList()
                .get(0)
                .getDataset()
                .getName();
    }

    private String getConfigName() {
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
            datasetName = getDatasetName();
            configName = getConfigName();
        }

        annotationDatasetName = datasetName;
        annotationConfigName = configName;

        Attribute a2 = Utils.getAttributeForAnnotationRetrieval(attrListElem);
        String key = annFilePathMapKey(datasetName, configName, a2.getName()),
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

            try (org.biomart.dino.SkipEmptyOutputStream oStream =
                    new org.biomart.dino.SkipEmptyOutputStream(new FileOutputStream(annotationFile))) {

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

        Log.debug(this.getClass().getName() + "#getAnnotationsFilePath() temp file "+ path);
        return path;
    }


//    private boolean isExternalDataset(Attribute attributeListElem) {
//        Attribute a2 = Utils.getAttributeForAnnotationRetrieval(attributeListElem);
//        return a2.getPointedDatasetName() != null
//               || ! a2.getPointedDatasetName().isEmpty();
//    }

    private boolean isSpecieTranslation(Attribute attrListElem) {
        Log.debug(this.getClass().getName() + "#isSpecieTranslation("+attrListElem.getName() + ")");

        Attribute a1 = Utils.getAttributeForEnsemblGeneIdTranslation(attrListElem);

        Log.debug(this.getClass().getName() + "#isSpecieTranslation() a1 name = "+ a1.getName());

        return a1.getName().contains("homolog");
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
    private String annFilePathMapKey(String datasetName,
                                     String datasetConfigName,
                                     String attributeName) {
        return datasetName + datasetConfigName + attributeName;
    }

    private void submitAnnotationsQuery(String dataset,
                                        String config,
                                        String attribute,
                                        OutputStream o) {
        initQueryBuilder();
        qbuilder.setDataset(dataset, config)
                .addAttribute("ensembl_gene_id")
                .addAttribute(attribute)
                .getResults(o);
    }

    private void submitToEnsemblIdQuery(Attribute transAttr,
                                        String filterName,
                                        String filterValue,
                                        OutputStream o) {

        initQueryBuilder();
        qbuilder.setDataset(getDatasetName(), getConfigName())
        .addAttribute(transAttr.getName())
        .addFilter(filterName, filterValue)
        .getResults(o);
    }

    private void initQueryBuilder() {
        qbuilder.init()
            .setUseDino(false);
    }

    private ByteArrayOutputStream byteStream() {
        return new ByteArrayOutputStream();
    }

    public String toString() {
        return "EnrichmentDino(background = " + background + "  " + "sets = "
                + sets + "  " + "annotation = " + annotation + "  "
                + "cutoff = " + cutoff + "  " + "client = " + client + ")";
    }

//    private String toString(ByteArrayOutputStream o)
//            throws UnsupportedEncodingException {
//        return o.toString("UTF-8");
//    }

    @Override
    public Dino setQuery(org.biomart.queryEngine.Query query) {
        q = query;
        client = this.q.getClient();

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

    private boolean isGuiClient() {
        return Boolean.valueOf(client) || client.equalsIgnoreCase("webbrowser");
    }

//    private List<QueryElement>
//    fromAttributeToQueryElementList(QueryElement source, List<Attribute> attrs) {
//        List<QueryElement> qs = new ArrayList<QueryElement>(attrs.size());
//        Dataset dataset = source.getDataset();
//
//        for (Attribute e : attrs)
//            qs.add(new QueryElement(e, dataset));
//
//        return qs;
//    }

}









































