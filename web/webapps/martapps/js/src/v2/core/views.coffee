_("BM.views").namespace (self) ->
  self.GuiContainerView = Backbone.View.extend(
    initialize: (options) ->
      _.bindAll this, "render", "update"
      @model.bind "change", @update
      @model.bind "change", @render
      @model.view = this

    update: ->
      @model.marts.reset @model.get("marts")

    render: ->
      @$(".guiContainerName").text @model.get("displayName")
      this
  )
  self.SelectBoxView = Backbone.View.extend(
    multiple: false
    initialize: (options) ->
      _.bindAll this, "render", "select"
      @collection = options.collection
      @collection.bind "reset", @render
      @_optionViews = []
      
      # Render initial select box
      @el.append render.selectBox(
        label: options.label
        id: options.id
        className: options.className
      )
      @$select = @$("select")

    events:
      "change select": "select"

    render: ->
      i = 0
      @$select.prettybox("destroy").empty()
      @collection.each _((that, model) ->
        view = new BM.views.SelectBoxOptionView(model: model)
        el = view.render().el.appendTo(that.$select)
        el.attr "selected", true  if i is 0
        i++
      ).partial(this)
      unless @multiple
        @$select.attr "multiple", false
        @$select.prettybox()
      else
        @$select.attr "multiple", true
      @$select.trigger "change"
      this # for chaining

    select: (ev) ->
      value = @$select.val()
      @collection.each (model) ->
        name = model.get("name")
        if (_.isArray(value) and _.include(value, name)) or name is value
          model.set selected: true
        else
          model.set selected: false

      @trigger "select"
  )
  self.SelectBoxOptionView = Backbone.View.extend(
    initialize: (options) ->
      _.bindAll this, "render"
      @model.bind "change", @render

    render: ->
      @el = $("<option>" + @model.get("name") + "</option>")
      this
  )
  self.AttributeView = Backbone.View.extend(
    tagName: "li"
    className: "model-attribute clearfix"
    _activeClassName: "model-attribute-active"
    events:
      "change input[type=checkbox]": "updateAttribute"

    initialize: (options) ->
      _.bindAll this, "updateCheckbox"
      @model.bind "change", @updateCheckbox

    render: ->
      render.attribute(@model.toJSON()).appendTo @el
      $(@el).addClass("model-attribute-" + @model.get("name")).data "view", this
      this

    updateAttribute: (evt) ->
      @model.set selected: evt.target.checked
      if @model.get("selected")
        $(@el).addClass @_activeClassName
        @trigger "add",
          model: @model

      else
        $(@el).removeClass @_activeClassName
        @trigger "remove",
          model: @model

      log "AttributeView: Updating attribute model", @model.get("displayName")

    updateCheckbox: ->
      input = @$("input[type=checkbox]")[0]
      input.checked = @model.get("selected")
      log "AttributeView: Updating attribute checkbox", @model.get("displayName")
  )
  self.FilterView = Backbone.View.extend(
    tagName: "li"
    className: "model-filter clearfix"
    _activeClassName: "model-filter-active"
    events:
      "click .filter-remove": "removeFilter"
      "change .filter-field": "validateFilter"

    initialize: (options) ->
      _.bindAll this, "render", "updateFilter"
      @model.bind "change:selected", @updateFilter

    render: ->
      valueList = @model.get("values")
      filterList = @model.filterList.toJSON()
      
      # Helper variables for template 
      render.filter(_.extend(@model.toJSON(),
        filters: filterList
        isValid: @isValid()
        hasText: @hasTextField()
        isMultiple: @isMultiple()
        hasUpload: @hasUploadField()
      )).appendTo @el
      $(@el).addClass ["filter-", @model.get("type"), " model-filter-", @model.get("name")].join("")
      
      # Add an invalid "Choose" option to select boxes
      @$("select:not([multiple])").prepend(["<option value=\"\">-- ", _("select").i18n(BM.i18n.CAPITALIZE), " --</option>"].join("")).val("").prettybox()
      @$(".filter-item-name").append(":").bind "click.simplerfilter", ->
        
        # prevent checkbox from being checked/unchecked
        false

      @$(".filter-field-text").addClass("ui-state-default ui-corner-all").bind "focus.simplerfilter", ->
        $(this).select()

      @$(".filter-field-upload-file").uploadify()
      @$closeButton = $(["<span class=\"ui-icon ui-icon-circle-close filter-remove\" title=\"", _("remove").i18n(BM.i18n.CAPITALIZE), "\"></span>"].join("")).hide().appendTo(@el)
      this

    removeFilter: ->
      log "FilterView: Removing filter", @model.get("displayName")
      @model.set
        selected: false
        value: null

      @$(".filter-field").val ""
      @$(".ui-autocomplete-input").val ["-- ", _("select").i18n(BM.i18n.CAPITALIZE), " --"].join("")

    
    # TODO: Need some validation here
    validateFilter: ->
      value = @getValue()
      if value
        log "FilterView: Setting filter", @model.get("displayName"), value
        @model.set
          selected: true
          value: value

      else
        log "FilterView: Removing filter", @model.get("displayName")
        @model.set
          selected: false
          value: null


    updateFilter: ->
      log "FilterView: Updating filter view selection", @model.get("displayName")
      if @model.get("selected")
        $(@el).addClass @_activeClassName
        @$closeButton.show()
        @trigger "add",
          model: @model

      else
        $(@el).removeClass @_activeClassName
        @$closeButton.hide()
        @trigger "remove",
          model: @model


    _validationHandlers:
      text: ->
        true

      upload: ->
        true

      singleSelect: (model) ->
        !!model.get("values").length

      singleSelectBoolean: (model) ->
        !!model.filterList.length

      singleSelectUpload: (model) ->
        !!model.filterList.length

      multiSelect: (model) ->
        !!model.get("values").length

      multiSelectBoolean: (model) ->
        !!model.filterList.length

      multiSelectUpload: (model) ->
        !!model.filterList.length

    isValid: ->
      type = @model.get("type")
      handler = @_validationHandlers[type]
      return handler(@model)  if handler
      false

    hasUploadField: ->
      type = @model.get("type")
      /upload/i.test type

    hasTextField: ->
      type = @model.get("type")
      type is "text"

    isMultiple: ->
      type = @model.get("type")
      /multi/i.test type

    getValue: ->
      @$(".filter-value").val()
  )
  self.AttributeListView = Backbone.View.extend(
    tagName: "ul"
    className: "collection-attribute clearfix"
    initialize: (options) ->
      _.bindAll this, "render"
      @collection = options.collection
      @collection.bind "reset", @render

    render: ->
      @collection.each _((that, model) ->
        view = new BM.views.AttributeView(model: model)
        view.bind "all", (evtName, data) ->
          that.trigger evtName, data

        $(view.render().el).appendTo that.el
      ).partial(this)
      this
  )
  self.FilterListView = Backbone.View.extend(
    tagName: "ul"
    className: "collection-filter clearfix"
    initialize: (options) ->
      _.bindAll this, "render"
      @collection = options.collection
      @collection.bind "reset", @render

    render: ->
      @collection.each _((that, model) ->
        view = new BM.views.FilterView(model: model)
        $(view.render().el).appendTo that.el
        view.bind "all", (evtName, data) ->
          that.trigger evtName, data

      ).partial(this)
      this
  )
  self.ContainerView = Backbone.View.extend(
    initialize: (options) ->
      _.bindAll this, "render"
      @model.bind "change", @render
      @_subContainerViews = []

    render: ->
      that = this
      filterList = @model.filterList
      attributeList = @model.attributeList
      containerList = @model.containerList
      @$rendered.remove()  if @$rendered
      
      # Render THIS container
      @$rendered = render.container(@model.toJSON()).appendTo(@el)
      
      # Render attributes
      if attributeList.length
        @_attributesView = new BM.views.AttributeListView(collection: attributeList)
        @_attributesView.bind "all", (evtName, data) ->
          that.trigger evtName, data

        @$rendered.append @_attributesView.render().el
      
      # Render filters
      if filterList.length
        @_filtersView = new BM.views.FilterListView(collection: filterList)
        @_filtersView.bind "all", (evtName, data) ->
          that.trigger evtName, data

        @$rendered.append @_filtersView.render().el
      
      # Render sub containers
      @model.containerList.each (container) ->
        newView = new BM.views.ContainerView(
          el: that.$rendered
          model: container
        ).render()
        newView.bind "all", (evtName, data) ->
          that.trigger evtName, data

        that._subContainerViews.push newView

      this
  )
  
  #
  #     * Paginator view provides page links for a Paginator object.
  #     *
  #     * Events triggers:
  #     *     "page" - passes the page number (one-indexed) as the argument
  #     
  self.PaginatorView = Backbone.View.extend(
    tagName: "p"
    className: "model-paginator"
    _defaultPagesToDisplay: 10
    events:
      "click .ui-page": "page"

    initialize: ->
      _.bindAll this, "render"
      @model.bind "change", @render
      @$el = $(@el)

    render: (model) ->
      log "PaginatorView.render()"
      currPage = model.get("currPage")
      pages = model.get("pages")
      options = pages: []
      last = _.last(pages)
      start = undefined
      end = undefined
      
      # no pages to render
      return  unless pages.length
      
      # Put currPage in the middle of page links
      start = Math.max(1, currPage - Math.floor(@_defaultPagesToDisplay / 2) + 1)
      
      # If start + (# pages to display) puts us out of range
      start = Math.max(1, last - @_defaultPagesToDisplay + 1)  if start + @_defaultPagesToDisplay > last
      end = Math.min(_.last(pages), start + @_defaultPagesToDisplay)
      
      # Figure out prev and next pages if applicable
      options.prevPage = currPage - 1  if currPage > 1
      options.nextPage = currPage + 1  if currPage < last
      
      # Populate pages array
      i = start

      while i <= end
        options.pages.push
          num: i
          isActive: i is currPage

        
        # Only display a set number of pages for pagination
        break  if options.pages.length >= @_defaultPagesToDisplay
        i++
      
      # Attach rendered pages to view element
      @$el.html render.pages(options)
      this # for chaining

    page: (evt) ->
      clicked = $(evt.target)
      num = clicked.data("page")
      log "Page clicked", num
      @model.set currPage: num
      @trigger "page", num
  )
