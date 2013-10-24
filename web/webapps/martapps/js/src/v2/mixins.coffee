# Custom utility functions
_.mixin
  
  # Create namespace
  namespace: (str, obj) ->
    p = window
    ns = str.split(".")
    i = 0
    curr = undefined

    while curr = ns[i]
      p = p[curr] = p[curr] or {}
      i++
    _.extend p, (if _.isFunction(obj) then obj(p) else obj)  if obj

  
  # Partial application
  partial: (fn) ->
    curryArgs = Array::slice.call(arguments_, 1)
    ->
      normalizedArgs = Array::slice.call(arguments_, 0)
      fn.apply null, curryArgs.concat(normalizedArgs)

  
  # i18n labels
  i18n: (label, o) ->
    key = label.replace(/\s+/g, "_")
    str = BM.conf.labels[key] or label
    plural = undefined
    if o & BM.i18n.PLURAL
      plural = BM.conf.labels[key + "__plural"]
      if plural
        str = plural
      else
        str = str + "s"
    str = str.charAt(0).toUpperCase() + str.slice(1)  if o & BM.i18n.CAPITALIZE
    str

  
  # Strips out any invalid character for class names
  slugify: (str) ->
    str.replace BM.CLASS_NAME_REGEX, ""

  
  # Escapes XML entities
  escapeXML: (str) ->
    el = $("<span/>")
    el.text str
    el.html()
