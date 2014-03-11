(function (angular) {
"use strict";

var app = angular.module("martVisualEnrichment.directives");

app.directive("loading", ["$rootScope", function ($rootScope) {
    return {
        restrict: "E",
        template: '<div class="loading">Loading...</div>',
        link: function (scope, elem, attrs) {
            $rootScope.$on("enrichment.dataloading", function () {
                elem.css({
                    visibility: "visible",
                    position: "absolute",
                    left: "40px",
                    top: "40px"
                });
            })
            $rootScope.$on("enrichment.dataloaded", function () {
                elem.css({
                    visibility: "hidden"
                });
            })
        }
    }
}])

})(angular);