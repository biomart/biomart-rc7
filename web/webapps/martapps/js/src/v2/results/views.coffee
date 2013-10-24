_("BM.results.views").namespace (self) ->
  window.BM_callbacks = {}
  self.ResultsPanelView = Backbone.View.extend(
    _hiddenClassName: "hidden"
    events:
      "click .biomart-results-show": "showResults"
      "click .biomart-results-hide": "hideResults"

    _isOnHeaders: false
    initialize: (options) ->
      _.bindAll this, "callback", "done"
      @_iframeId = _.uniqueId("BioMart")
      
      # If data view is not specified, use plaintext
      @_dataViewClass = options.dataViewClass or self.PlainDataView
      @_dataViewOptions = options.dataViewOptions or {} # set options if exist
      @_dataModel = new BM.results.models.ResultData() # instantiate data model
      @_dataView = new @_dataViewClass(_.extend( # instantiate data view
        model: @_dataModel
      , @_dataViewOptions))
      @$content = $(@el).find(".biomart-results-content").addClass(@_hiddenClassName)
      @$loading = $("<span class=\"loading\"/>").hide().appendTo(@el)
      
      # iframe for streaming results from server
      @$iframe = $("<iframe class=\"streaming\" name=\"" + @_iframeId + "\"/>").appendTo(@$content)
      @$submitButton = @$(".biomart-results-show")
      @$backButton = @$(".biomart-results-hide")
      
      # form for submitting query
      @$form = $("<form class=\"streaming\" method=\"POST\" target=\"" + @_iframeId + "\" " + "action=\"" + BM.conf.service.url + "results\">" + "<input type=\"hidden\" name=\"query\"/>" + "<input type=\"hidden\" name=\"iframe\" value=\"true\"/>" + "<input type=\"hidden\" name=\"scope\" value=\"" + "BM_callbacks." + @_iframeId + "\"/>" + "<input type=\"hidden\" name=\"uuid\" value=\"" + @_iframeId + "\"/>" + "</form>").appendTo(@$content)
      window.BM_callbacks[@_iframeId] =
        write: @callback
        done: @done

    callback: (uuid, row) ->
      
      # Check that the row is not empty
      if row
        row = row.split("\t")
        if @_isOnHeaders
          @_dataModel.set header: row
          @_isOnHeaders = false
        else
          @_dataModel.addRow row

    done: (uuid) ->
      @_dataModel.done()
      @$loading.hide()

    showResults: ->
      @trigger "show"
      
      # Whether we need to handle headers first
      @_isOnHeaders = true  if @model.get("header")
      @$content.append(@_dataView.render().el).removeClass @_hiddenClassName
      @$form.find("input[name=query]").val(@model.compileForPreview()).end().submit()
      
      # Switch user controls
      @$submitButton.hide()
      @$backButton.show()
      @$loading.show()
      this

    hideResults: ->
      @trigger "hide"
      @$content.addClass @_hiddenClassName
      
      # Reset controls
      @$submitButton.show()
      @$backButton.hide()
      
      # Clear out results
      @_dataModel.reset()
      this
  )
  
  #
  #     * PlainDataView renders a ResultData object in a <pre/> element in plaintext.
  #     *
  #     * Other data views extend this prototype.
  #     
  self.PlainDataView = Backbone.View.extend(
    tagName: "pre"
    className: "biomart-results-plaintext"
    _noDataClassName: "biomart-results-nodata"
    initialize: (options) ->
      _.bindAll this, "updateHeader", "displayRow", "done", "render", "clear"
      @$el = $(@el)
      @model.bind "change:header", @updateHeader
      @model.bind "add:row", @displayRow
      @model.bind "reset", @clear
      @model.bind "loaded", @done

    render: ->
      log "PlainDataView.render()"
      this

    updateHeader: (model) ->
      @$el.append model.headers.join("\t") + "\n"

    displayRow: (model, row, index) ->
      @$el.append row.join("\t") + "\n"

    done: (model) ->
      log "PlainDataView.done()"
      
      # If no data returned, show "No data" message
      @$el.html(_("no data").i18n(BM.i18n.CAPITALIZE)).addClass @_noDataClassName  if model.getTotal() is 0

    clear: ->
      log "PlainDataView.clear()"
      @$el.removeClass @_noDataClassName
      @$thead.empty()
      @$tbody.empty()
  )
  
  #
  #     * Displays ResultData as a <table/> element.
  #     *
  #     * Supports pagination if the total rows returned > pageSize.
  #     
  self.TableDataView = self.PlainDataView.extend(
    tagName: "table"
    className: "biomart-results-table"
    events:
      "click .ui-table-sortable": "sortResultData"

    _currPage: 1 # Cache of paginator's current page (for faster row display)
    _pageSize: 20 # Default to 20 rows per page
    initialize: (options) ->
      _.bindAll this, "_scrollToPage", "_updateCurrPage", "_updateResultsInfo", "_displaySortedData"
      @__super__ "initialize"
      if "pageSize" of options
        log "Paginating by " + options.pageSize
        @_pageSize = options.pageSize
      @_paginatorModel = new BM.models.Paginator()
      @_paginatorView = new BM.views.PaginatorView(model: @_paginatorModel)
      @$thead = $("<thead/>").appendTo(@$el)
      @$tbody = $("<tbody/>").appendTo(@$el)
      
      # This ordering is important because _currPage needs to be updated first before all other callbacks
      @_paginatorModel.bind "change:currPage", @_updateCurrPage
      @_paginatorModel.bind "change:currPage", @_scrollToPage
      @_paginatorModel.bind "change:currPage", @_updateResultsInfo
      @model.bind "sort:rows", @_displaySortedData

    render: ->
      @__super__ "render"
      this

    clear: ->
      @__super__ "clear"
      log "TableDataView.clear()"
      if @$info
        @$info.remove()
        delete @$info
      @_paginatorModel.reset()
      @_paginatorView.$el.empty()
      @model.reset silent: true
      delete @_sortedColumnIndex

    updateHeader: (model) ->
      arr = []
      header = model.get("header")
      if header
        i = 0

        while i < header.length
          arr.push
            text: header[i]
            sortable: true

          i++
        @$thead.append render.tableHeader(header: arr)

    displayRow: (model, row, index) ->
      
      # Figure out start and end ranges (if pageSize is set)
      # Range is inclusive i.e. [start, end] not (start, end)
      if @_pageSize
        range = @_getRowsRange()
        
        # Don't dislay if row index falls outside of range
        return  if index < range.start or index > range.end
      @_appendRow row

    
    # Appends the row array to the table
    _appendRow: (row) ->
      
      # If we know how many columns to expect, then initialize array to that size.
      # This will allow use to print empty columns even if the data doesn't come back.
      arr = []
      i = 0

      while i < row.length
        arr.push row[i]
        i++
      @$tbody.append render.tableRow(row: arr)

    done: (resultDataModel) ->
      @__super__ "done", resultDataModel
      log "TableDataView.done()"
      total = resultDataModel.getTotal()
      pages = []
      
      # Make sure we have results to display
      if total > 0
        range = @_getRowsRange()
        
        # Display meta info
        @$info = render.resultsMeta(
          start: range.start
          end: range.end
          total: total
          limit: BM.PREVIEW_LIMIT # defined in core.js
          hasMoreData: total >= BM.PREVIEW_LIMIT # We have to assume that there are
        )
        # more data available if we've reached
        # the limit. It is possible that 
        # the total results == limit though.
        @$el.before @$info
        
        # If we have more rows than the page size then display pagination links
        if @_pageSize and total >= @_pageSize
          log "Total rows: ", total
          i = 0 # initial page index
          loop
            pages.push ++i # create one-based indices
            break unless (total -= @_pageSize) > 0
          @_paginatorModel.set pages: pages
          @$el.after @_paginatorView.el
      else

    
    # TODO: handle empty case
    _sortClassNames:
      asc: "ui-table-ascending"
      desc: "ui-table-descending"

    sortResultData: (evt) ->
      log "TableDataView.sortResultData()"
      $target = $(evt.target)
      $column = $target.closest(".ui-table-sortable")
      isCurrentlyAscending = $column.hasClass(@_sortClassNames.asc)
      index = $column.index()
      
      # Remove sort class names from sibling elements
      that = this
      $column.siblings().each (index, element) ->
        $(element).removeClass(that._sortClassNames.asc).removeClass that._sortClassNames.desc

      
      # Toggle between ascending and descending class names
      if isCurrentlyAscending
        $column.removeClass(@_sortClassNames.asc).addClass @_sortClassNames.desc
      else
        $column.removeClass(@_sortClassNames.desc).addClass @_sortClassNames.asc
      
      # Sort ResultData
      @model.sort index, not isCurrentlyAscending
      @_sortedColumnIndex = index
      @_highlightColumn index

    
    #
    #         * Highlights the <td/> element specified by the index (zero-based)
    #         
    _highlightColumn: (columnIndex) ->
      @$("tr").each (index, element) ->
        $columns = $(element).children("td")
        $columns.removeClass "ui-table-highlight"
        $columns.eq(columnIndex).addClass "ui-table-highlight"


    
    #
    #         * Returns the start and end row indicies based on current page number
    #         
    _getRowsRange: ->
      start = (@_currPage - 1) * @_pageSize + 1
      end = Math.min(start + @_pageSize - 1, @model.getTotal())
      start: start
      end: end

    
    #
    #         * Callback when Paginator's currPage attribute changes. Keeps cached _currPage in sync.
    #         
    _updateCurrPage: (paginatorModel, newPage) ->
      @_prevPage = @_currPage
      @_currPage = newPage

    
    # 
    #         * Scrolls to the new page
    #         
    _scrollToPage: (paginatorModel, newPage) ->
      direction = (if newPage < @_prevPage then 1 else -1) # used for setting left margin
      log "Displaying table page", @_currPage
      w = @$el.outerWidth() + 100
      $parent = @$el.parent()
      
      # Temporarily fix the height for smoother animation
      $parent.height $parent.height()
      that = this
      @$el.animate
        marginLeft: w * direction
      ,
        duration: 100
        complete: ->
          that._refreshCurrentPage()
          
          # Put table on the other side
          that.$el.css marginLeft: w * direction * -1
          
          # Bring table back
          that.$el.animate
            marginLeft: 0
          ,
            duration: 100
            complete: ->
              $parent.height "auto"



    
    #
    #         * Refresh results from the ResultData for the current page. Used when pagination or sorting occurs.
    #         
    _refreshCurrentPage: ->
      start = (@_currPage - 1) * @_pageSize
      rows = @model.getRows(start, @_pageSize) # model is ResultData
      @$tbody.empty() # clear out old results
      # Scroll the page out of sight
      i = 0
      row = undefined

      while row = rows[i]
        @_appendRow row
        i++
      @_highlightColumn @_sortedColumnIndex  if "_sortedColumnIndex" of this

    
    #
    #         * Updates the info text
    #         
    _updateResultsInfo: (paginatorModel) ->
      if @$info
        range = @_getRowsRange()
        @$info.find(".start").text range.start
        @$info.find(".end").text range.end

    
    #
    #         * Callback for when user clicks on a column header
    #         
    _displaySortedData: (resultDataModel, index, ascending) ->
      
      # Go back to page 1, which will also display the sorted results
      @_paginatorModel.set currPage: 1
      @_refreshCurrentPage()
  )
