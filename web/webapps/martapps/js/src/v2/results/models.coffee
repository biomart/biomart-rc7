_("BM.results.models").namespace (self) ->
  self.ResultData = Backbone.Model.extend(
    defaults:
      header: null
      rows: []

    reset: (options) ->
      silent = (if options then !!options.silent else false)
      @set
        header: null
        rows: []
      ,
        silent: silent

      @trigger "reset"  unless silent
      this

    
    #
    #         * Argument row should be an array
    #         
    addRow: (row) ->
      newLength = @get("rows").push(row)
      
      # Triggers with model arguments[0]
      @trigger "change:rows", this
      
      # Triggers with model arguments[0], the new row arguments[1], and new index arguments[2]
      @trigger "add:row", this, row, newLength - 1
      this

    getRows: (offset, numRows) ->
      offset = offset or 0
      return @get("rows").slice(offset)  unless numRows
      @get("rows").slice offset, offset + numRows

    _htmlRegex: /<.+?>/g
    
    #
    #         * Sorts the ResultData by the column specified by index (zero-based).
    #         *
    #         * If ascending is true then sort by ascending order, descending otherwise. Default when
    #         * not specified is descending.
    #         
    sort: (index, ascending) ->
      rows = @get("rows")
      sorted = rows.sort((left, right) ->
        a = left[index].replace(@_htmlRegex, "").toUpperCase()
        b = right[index].replace(@_htmlRegex, "").toUpperCase()
        return (if ascending then 1 else -1)  if a > b
        return (if ascending then -1 else 1)  if a < b
        0
      )
      @set # silent because we'll trigger sortedBy change
        rows: sorted
      ,
        silent: true

      @trigger "sort:rows", this, index, ascending
      this

    getTotal: ->
      @get("rows").length

    done: ->
      @trigger "loaded", this
      this
  )
