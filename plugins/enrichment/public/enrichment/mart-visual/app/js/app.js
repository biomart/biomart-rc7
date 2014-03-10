;(function (angular) {
"use strict";


angular.module("martVisual", [
    "ui.bootstrap",
    "martVisual.services",
    "martVisual.controllers",
    "martVisual.directives"
]).

run(["$templateCache" ,function($templateCache) {
    $templateCache.put("partials/martvisual.html", document.getElementById("martvisual.html").textContent);
    $templateCache.put("partials/bool-filter.html", document.getElementById("bool-filter.html").textContent);
    $templateCache.put("partials/text-filter.html", document.getElementById("text-filter.html").textContent);
    $templateCache.put("partials/upload-filter.html", document.getElementById("upload-filter.html").textContent);
    $templateCache.put("partials/single-select-upload-filter.html", document.getElementById("single-select-upload-filter.html").textContent);
    $templateCache.put("partials/container.html", document.getElementById("container.html").textContent);
    $templateCache.put("partials/containerset.html", document.getElementById("containerset.html").textContent);
    $templateCache.put("partials/bio-attribute.html", document.getElementById("bio-attribute.html").textContent);
}]);

})(angular);