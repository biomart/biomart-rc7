#
# * This modules containers the core BioMart models and collections
# 
_("BM.models").namespace (self) ->
  
  #
  #     * Models
  #     
  self.operations =
    MULTI_SELECT: "MULTISELECT"
    SINGLE_SELECT: "SINGLESELECT"

  self.GuiContainer = Backbone.Model.extend(
    url: ->
      BM.conf.service.url + ((if @name then "gui?name=" + @name else "portal"))

    initialize: (options) ->
      options.name and (@name = options.name)
      @marts = new BM.models.MartList
  )
  self.Mart = Backbone.Model.extend(defaults:
    selected: false
    config: "default"
  )
  self.Dataset = Backbone.Model.extend(defaults:
    selected: false
  )
  self.Filter = Backbone.Model.extend(
    initialize: ->
      @set
        selected: false
        value: null
      ,
        silent: true

      @filterList = new BM.models.FilterList

    parse: (resp) ->
      that = this
      @set
        id: that.cid
        name: resp.name
        displayName: resp.displayName
        description: resp.description
        type: resp.type
        isHidden: resp.isHidden
        values: resp.values
      ,
        silent: true

      _.each resp.filters, (filter) ->
        newFilter = new BM.models.Filter
        newFilter.parse filter
        that.filterList.add newFilter

      this
  )
  self.Attribute = Backbone.Model.extend(
    defaults:
      selected: false

    initialize: ->
      @attributeList = new BM.models.AttributeList

    parse: (resp) ->
      @set
        id: @cid
        name: resp.name
        displayName: resp.displayName
        description: resp.description
        isHidden: resp.isHidden
        value: resp.value
        linkURL: resp.linkURL
      ,
        silent: true

      this
  )
  self.Container = Backbone.Model.extend(
    url: ->
      params =
        datasets: @get("datasets")
        withattributes: @get("withattributes")
        withfilters: @get("withfilters")

      config = undefined
      params.config = config  if config = @get("mart").get("config")
      BM.conf.service.url + "containers?" + $.param(params)

    initialize: ->
      _.bindAll this, "parse"
      @containerList = new BM.models.ContainerList
      @attributeList = new BM.models.AttributeList
      @filterList = new BM.models.FilterList

    parse: (resp) ->
      that = this
      @set
        id: that.cid
        name: resp.name
        displayName: resp.displayName
        independent: resp.independent
        description: resp.description
        maxContainers: resp.maxContainers
        maxAttributes: resp.maxAttributes
      ,
        silent: true

      @containerList.reset _.map(resp.containers, (container) ->
        new BM.models.Container().parse container
      )
      @attributeList.reset _.map(resp.attributes, (attribute) ->
        new BM.models.Attribute().parse attribute
      )
      @filterList.reset _.map(resp.filters, (filter) ->
        new BM.models.Filter().parse filter
      )
      this
  )
  
  # 
  #     * One-based index of pages
  #     
  self.Paginator = Backbone.Model.extend(
    defaults:
      pages: []
      currPage: 1

    reset: (options) ->
      silent = (if options then !!options.silent else false)
      @set
        pages: []
        currPage: 1
      ,
        silent: silent

  )
  
  # 
  #     * Collections
  #     
  self.MartList = Backbone.Collection.extend(
    model: BM.models.Mart
    url: BM.conf.service.url + "marts"
    selected: ->
      @detect (mart) ->
        mart.get "selected"

  )
  self.DatasetList = Backbone.Collection.extend(
    model: BM.models.Dataset
    url: BM.conf.service.url + "datasets"
    initialize: (options) ->
      @mart = options.mart  if options and options.mart

    selected: ->
      @models.filter (ds) ->
        !!ds.get("selected")


    hasSelected: ->
      @models.some (ds) ->
        !!ds.get("selected")


    toString: ->
      arr = []
      @models.forEach (ds) ->
        arr.push ds.escape("name")  if ds.get("selected")

      arr.join ","
  )
  self.AttributeList = Backbone.Collection.extend(
    model: BM.models.Attribute
    url: BM.conf.service.url + "attributes"
  )
  self.FilterList = Backbone.Collection.extend(
    model: BM.models.Filter
    url: BM.conf.service.url + "filters"
  )
  self.ContainerList = Backbone.Collection.extend(model: BM.models.Container)
