;(function (angular) {
"use strict";

angular.module("martVisualEnrichment.directives").


directive("mvResultsTable",
          ["$rootScope",
          function ($rootScope) {
    return {
        restrict: "E",
        templateUrl: "partials/table-of-results.html",
        link: function (scope, iElement, attrs) {
            function ln(evtName) {
                return function (evt) {
                    var el = null, t = evt.target;
                    if (t.tagName === "TD") {
                        el = t.parentNode;
                    }
                    else if (t.tagName === "TR") {
                        el = t;
                    }

                    if (el) {
                        var e = angular.element(t);
                        $rootScope.$emit(evtName, e.scope().term);
                    }
                }
            }

            iElement.on("mouseover", ln("term.mouseover"));
            iElement.on("mouseout", ln("term.mouseout"));
        }
    }
}]);

})(angular);