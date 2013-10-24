_("BM.query.models").namespace (self) ->
  
  #
  #     * A QueryElement contains a list of datasets + config, a list of attributes, 
  #     * and a list of filters. Equal to the <Dataset/> element in query XML.
  #     *
  #     * Can compile down to XML, Java, and SPARQL queries.
  #     *
  #     * TODO: Implement Java and SPARQL compilation
  #     
  self.QueryElement = Backbone.Model.extend(
    sync: (method, model, success, error) ->

    
    # no service for this right now
    # TODO: move this to localStorage?
    url: "/queryelement" # This is meaningless right now due to lack of sync()
    initialize: (options) ->
      log "QueryElement: initialize", options.config
      _.bindAll this, "_propagateAttributeEvent", "_propagateFilterEvent"
      @set id: options.config
      @datasetList = new BM.models.DatasetList(@get("datasets"))
      @filterList = new BM.models.FilterList
      @attributeList = new BM.models.AttributeList

      @filterList.bind "all", @_propagateFilterEvent
      @attributeList.bind "all", @_propagateAttributeEvent

    _propagateAttributeEvent: (eventName, model) ->
      @trigger "attribute:" + eventName, model

    _propagateFilterEvent: (eventName, model) ->
      @trigger "filter:" + eventName, model

    compile: (format) ->
      fn = @_compileFunctions[format]
      if fn
        fn.apply this
      else
        throw "Could not find compile function for format: " + format

    
    #
    #         * Compiles the query into formats matched by key
    #         
    _compileFunctions:
      xml: ->
        arr = []
        datasets = @datasetList.toString()
        arr.push ["<Dataset name=\"", datasets, "\"", " config=\"" + @escape("config") + "\"", ">"].join("")
        @filterList.each (filter) ->
          arr.push ["<Filter name=\"", filter.escape("name"), "\"", " value=\"", filter.escape("value"), "\"/>"].join("")

        @attributeList.each (attribute) ->
          arr.push ["<Attribute name=\"", attribute.escape("name"), "\"/>"].join("")

        arr.push "</Dataset>"
        arr.join ""

      java: ->
        arr = []
        datasets = @datasetList.toString()
        arr.push ["\n        Query.Dataset ds = query.addDataset(\"", datasets, "\", ", (if @escape("config") then ("\"" + @escape("config") + "\"") else "null"), ");"].join("")
        @filterList.each (filter) ->
          arr.push ["        ds.addFilter(\"", filter.escape("name"), "\", \"", filter.escape("value"), "\");"].join("")

        @attributeList.each (attribute) ->
          arr.push ["        ds.addAttribute(\"", attribute.escape("name"), "\");"].join("")

        arr.join "\n"

      sparql: ->
        
        #
        #                 * Helper functions for SPARQL
        #                 
        site2reference = (siteURL) ->
          refURL = siteURL.replace(/^https:/, "biomart:")
          refURL = refURL.replace(/^http:/, "biomart:")
          refURL
        identifier2SPARQL = (id) ->
          
          # This regexp has to be identical to the regexp in ObjectController.createDefaultRDF
          id = "_" + id  unless id.match(/^[a-zA-Z_].*/)
          id
        arr = []
        config = @escape("config")
        datasets = @datasetList.toString()
        arr.push "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
        arr.push "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n"
        arr.push "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"
        arr.push "\n"
        arr.push "PREFIX accesspoint: <" + BIOMART_CONFIG.siteURL + "martsemantics/" + config + "/ontology#>\n"
        arr.push "PREFIX class: <" + site2reference(BIOMART_CONFIG.siteURL) + "martsemantics/" + config + "/ontology/class#>\n"
        arr.push "PREFIX dataset: <" + site2reference(BIOMART_CONFIG.siteURL) + "martsemantics/" + config + "/ontology/dataset#>\n"
        arr.push "PREFIX attribute: <" + site2reference(BIOMART_CONFIG.siteURL) + "martsemantics/" + config + "/ontology/attribute#>\n\n"
        arr.push "SELECT "
        @attributeList.each (attribute) ->
          name = identifier2SPARQL(attribute.escape("name"))
          arr.push "?" + name + " "

        arr.push "\n"
        @datasetList.each (dataset) ->
          arr.push "FROM dataset:" + dataset.escape("name") + "\n"

        arr.push "WHERE {\n"
        @filterList.each (filter) ->
          name = identifier2SPARQL(filter.escape("name"))
          arr.push "  ?x attribute:" + name + " \"" + filter.escape("value") + "\" .\n"

        i = 0
        that = this
        @attributeList.each (attribute) ->
          name = identifier2SPARQL(attribute.escape("name"))
          arr.push "  ?x attribute:" + name + " ?" + name
          if ++i < that.attributeList.length
            arr.push " .\n"
          else
            arr.push "\n"

        arr.push "}\n"
        return arr.join("")
  )
  
  #
  #     * Collection of QueryElements
  #     
  self.QueryElementList = Backbone.Collection.extend(model: BM.query.models.QueryElement)
  
  #
  #     * Represents the entire query; Contains a list of QueryElements.
  #     *
  #     * Can compile to XML, Java, and SPARQL.
  #     * TODO: Implement Java and SPARQL compilation
  #     *
  #     * Propagates events from QueryElementList:
  #     *  - add : new QueryElement added
  #     *  - remove : QueryElement removed
  #     *  - attribute:add : new Attribute added
  #     *  - attribute:remove : Attribute removed
  #     *  - filter:add : new Filter added
  #     *  - filter:remove : Filter removed
  #     
  self.Query = Backbone.Model.extend(
    defaults:
      processor: "TSV"
      limit: -1
      header: true
      client: "webbrowser"

    initialize: ->
      _.bindAll this, "_propagateEvent"
      @queryElements = new self.QueryElementList
      @queryElements.bind "all", @_propagateEvent

    _propagateEvent: (eventName) ->
      log "Query._propagateEvent", eventName
      @trigger.apply this, Array::slice.call(arguments_, 0)

    addElement: (queryElement) ->
      log "Query.addElement"
      @queryElements.add queryElement
      this

    removeElement: (queryElement) ->
      log "Query.removeElement"
      @queryElements.remove queryElement
      this

    getElement: (config) ->
      @queryElements.detect (element) ->
        element.get("config") is config


    
    #
    #         * Compiles the Query object into a string. Takes an optional **format**
    #         * argument -- default is XML.
    #         
    compile: (format) ->
      format = format or "xml"
      @_compileFunctions[format].call this

    
    #
    #         * Compile query for preview (i.e. with a limit)
    #         
    compileForPreview: (format) ->
      oldLimit = @get("limit")
      oldProcessor = @get("processor")
      @set
        limit: BM.PREVIEW_LIMIT
      ,
        silent: true

      
      # So we can see links
      if oldProcessor is "TSV"
        @set
          processor: "TSVX"
        ,
          silent: true

      compiled = @compile(format)
      @set
        limit: oldLimit
        processor: oldProcessor
      ,
        silent: true

      compiled

    _compileFunctions:
      xml: ->
        arr = []
        arr.push ["<Query processor=\"", @escape("processor"), "\" header=\"", @escape("header"), "\" limit=\"", @escape("limit"), "\"  client=\"", @escape("client"), "\">"].join("")
        @queryElements.each (queryElement) ->
          arr.push queryElement.compile("xml")

        arr.push "</Query>"
        arr.join ""

      java: ->
        arr = []
        arr.push "import org.biomart.api.factory.*;"
        arr.push "import org.biomart.api.Portal;"
        arr.push "import org.biomart.api.Query;\n"
        arr.push "/*"
        arr.push " * This is a runnable Java class that executes the query."
        arr.push " * Please adapt this code as needed, and DON'T forget to change the xmlPath."
        arr.push " */\n"
        arr.push "public class QueryTest {"
        arr.push "    public static void main(String[] args) throws Exception {"
        arr.push "        String xmlPath = \"/path/to/registry_xml\"; // Needs to be changed\n"
        arr.push "        MartRegistryFactory factory = new XmlMartRegistryFactory(xmlPath, null);"
        arr.push "        Portal portal = new Portal(factory, null);"
        arr.push "\n        Query query = new Query(portal);"
        arr.push ["        query.setProcessor(\"", @escape("processor"), "\");"].join("")
        arr.push ["        query.setClient(\"", @escape("client"), "\");"].join("")
        arr.push ["        query.setLimit(", @escape("limit"), ");"].join("")
        arr.push ["        query.setHeader(", @escape("header"), ");"].join("")
        @queryElements.each (queryElement) ->
          arr.push queryElement.compile("java")

        arr.push "\n        // Print to System.out, but you can pass in any java.io.OutputStream"
        arr.push "        query.getResults(System.out);"
        arr.push ["\n        System.exit(0);"].join("")
        arr.push "    }"
        arr.push "}"
        arr.join "\n"

      sparql: ->
        arr = []
        @queryElements.each (queryElement) ->
          arr.push queryElement.compile("sparql")

        arr.push "LIMIT " + @get("limit") + "\n"  if @get("limit") > 0
        arr.join ""
  )
