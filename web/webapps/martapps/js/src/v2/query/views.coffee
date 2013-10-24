_("BM.query.views").namespace (self) ->
  self.QueryToolbarView = Backbone.View.extend(
    tagName: "menu"
    className: "biomart-query ui-widget ui-widget-content ui-corner-all clearfix"
    events:
      "click .remove-dataset": "removeDataset"
      "click .remove-filter": "removeFilter"
      "click .remove-attribute": "removeAttribute"
      "click .biomart-compile-target": "showQuery"

    initialize: (options) ->
      
      # automatically render toolbar
      @render()

    render: ->
      log "QueryView.render"
      targets = [
        target: "xml"
        label: "REST/SOAP"
      ,
        target: "sparql"
        label: "SPARQL"
      ,
        target: "java"
        label: "Java"
      ]
      @_toolbar = render.query(compilationTargets: targets).prependTo(@el)
      @_dialogs = {}
      i = 0
      target = undefined

      while target = targets[i]
        @_dialogs[target.target] = new self.ExplainQueryView(target)
        i++
      this

    removeDataset: ->

    removeFilter: ->

    removeAttribute: ->

    showQuery: (evt) ->
      log "QueryInfoView.showQuery"
      $target = $(evt.target).closest(".biomart-compile-target")
      compilationTarget = $target.data("target")
      source = @model.compile(compilationTarget)
      @_dialogs[compilationTarget].source(source).open()

    remove: ->
      @__super__ "remove"
  )
  self.ExplainQueryView = Backbone.View.extend(
    tagName: "div"
    events:
      "click .close": "close"
      "click .escapeQuotes": "escapeQuotes"

    initialize: (options) ->
      @_target = options.target
      @_label = options.label

    source: (source) ->
      log "ExplainQueryView.source", source
      @_source = source
      this

    open: ->
      
      # Lazy creation of dialog when needed
      unless @$dialog
        @$dialog = render.queryDialog(title: @_label).appendTo(document.body).dialog(
          dialogClass: "biomart-query"
          autoOpen: false
          modal: true
          width: 700
          height: 450
          buttons:
            Close: ->
              $(this).dialog "close"
        )
      @$dialog.find("textarea").text(@_source).end().dialog "open"
      this

    close: ->
      @$dialog.dialog "close"
      this

    escapeQuotes: ->

    remove: ->
      @__super__ "remove"
      @$dialog.dialog("destroy").remove()  if @$dialog
  )
