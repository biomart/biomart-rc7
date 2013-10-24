#
# * Set log() function to whatever is available
# 
window.log = ->
  window.console.log arguments_  if window.console


#
# * Helper function for calling __super__ functions for Backbone's 
# * View and Model objects.
# 
Backbone.Model::__super__ = (funcName) ->
  @constructor.__super__[funcName].apply this, _.rest(arguments_)

Backbone.View::__super__ = (funcName) ->
  @constructor.__super__[funcName].apply this, _.rest(arguments_)


#
# * Core BioMart functions
# 
_("BM").namespace (self) ->
  self.NAME = "BioMart"
  self.SEPARATOR = "#!/"
  self.PREVIEW_LIMIT = 1000
  guid = 1
  regex = new RegExp(["(https?://[^/]+/?)?([a-zA-Z0-9._\\-/ %]+)?(\\?([^#]+))?(", self.SEPARATOR, "(.+))?"].join(""))
  self.i18n =
    CAPITALIZE: 1
    PLURAL: 2

  self.CLASS_NAME_REGEX = /[^a-zA-Z0-9_-]/g
  self.conf = $.extend({}, BIOMART_CONFIG)
  self.jsonify = (url) ->
    url = url or location.href
    match = url.match(regex)
    return false  unless match
    host: match[1]
    path: match[2] or ""
    query: match[4] or ""
    fragment: match[6] or ""

  self.stringify = (hash) ->
    arr = [hash.host, hash.path]
    arr.push ["?", hash.query].join("")  if hash.query
    arr.push ["#!/", hash.fragment].join("")  if hash.fragment
    arr.join ""

  
  # Very simple but very fast query param parsing.
  # If multiple entries exist for a param, the value of the first occurrence is used.
  # This is not normal behaviour, but is not allowed in our application anyway.
  self.simpleQueryParams = (s) ->
    return null  unless s
    arr = s.split("&")
    n = arr.length
    dict = {}
    while n--
      me = arr[n]
      me = me.split("=")
      dict[me[0]] = decodeURIComponent(me[1]).replace(/\+/g, " ")
    dict
