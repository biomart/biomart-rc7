;(function (angular) {
"use strict";

var app = angular.module("compApp", [
    "ngRoute",
    "martVisual",
    "martVisualEnrichment"
]);

app.config(["$routeProvider", "$locationProvider",
        function ($routeProvider, $locationProvider) {
    $routeProvider.when("/enrichment/", {
        templateUrl: "partials/martvisual.html",
        controller: "MartVisualCtrl"
    });
    $routeProvider.when("/enrichment/results", {
        templateUrl: "partials/mart-visual-enrichment.html",
        controller: "MartVisualEnrichmentCtrl"
    });
    $locationProvider.html5Mode(true);
}]);

})(angular);