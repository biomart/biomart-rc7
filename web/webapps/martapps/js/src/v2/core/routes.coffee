_("BM.routes").namespace (self) ->
  self.Router = Backbone.Router.extend(
    routes:
      "*params": "defaultRoute"

    defaultRoute: (params) ->
      log "defaultRoute", params
  )
