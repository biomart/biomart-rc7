;(function (angular) {
"use strict";

angular.module("martVisualEnrichment", [
    "martVisual",
    "martVisualEnrichment.services",
    "martVisualEnrichment.controllers",
    "martVisualEnrichment.directives"
]).

run(["$templateCache",function($templateCache) {
    $templateCache.put("partials/table-of-results.html", document.getElementById("table-of-results.html").textContent);
    $templateCache.put("partials/vis.html", document.getElementById("vis.html").textContent);
    $templateCache.put("partials/mart-visual-enrichment.html", document.getElementById("mart-visual-enrichment.html").textContent);
}]);

})(angular);