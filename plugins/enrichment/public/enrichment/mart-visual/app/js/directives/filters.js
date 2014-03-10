;(function (angular) {
"use strict";

function setTextarea(scope) {
    return function putText (evt) {
        var file = evt.target.files[0];
        if (file) {
            var reader = new FileReader();
            reader.readAsText(file, "UTF-8");
            reader.onload = function (evt) {
                scope.$apply(function () {
                    scope.textareaValue = evt.target.result.split("\n").join(",");
                });
            }
            reader.onerror = function (evt) {
                scope.$apply(function () {
                    scope.textareaValue = "Error reading file";
                });
            }
        }
    }
}


var app = angular.module("martVisual.directives");


app.directive("uploadFilter",
          ["queryBuilder", "sanitize", function (queryBuilder, sanitize) {
    return {
        restrict: "E",
        templateUrl: "partials/upload-filter.html",
        controller: function ($scope, $element, $attrs, $transclude) {},
        scope: {
            filter: "="
        },
        link: function (scope, iElement, attrs) {
            // iElement.find("input").on("change", getTextFromFileCb(iElement));
            iElement.find("input").on("change", setTextarea(scope));
            scope.$on("enrichment.results", function () {
                var t = scope.textareaValue, v = "";
                if (t) {
                    v = sanitize.stripTags(t);
                }
                queryBuilder.addFilter(scope.filter.name, v);
            });
        }
    }
}]);


app.directive("singleSelectUploadFilter",
          ["queryBuilder", "sanitize", function (queryBuilder, sanitize) {
    return {
        restrict: "E",
        templateUrl: "partials/single-select-upload-filter.html",
        controller: function ($scope, $element, $attrs, $transclude) {},
        scope: {
            filter: "="
        },
        link: function (scope, iElement, attrs) {
            scope.options = scope.filter.filters;
            scope.selected = scope.options[0];
            iElement.find("input").on("change", setTextarea(scope));
            scope.$on("enrichment.results", function () {
                var t = scope.textareaValue, v ="";
                if (t) {
                    v = sanitize.stripTags(t);
                }
                queryBuilder.addFilter(scope.selected.name, v);
            });
        }
    }
}]);


app.directive("textFilter",
          ["queryBuilder", "sanitize", function (queryBuilder, sanitize) {
    return {
        restrict: "E",
        templateUrl: "partials/text-filter.html",
        controller: function ($scope, $element, $attrs, $transclude) {},
        scope: {
            filter: "="
        },
        link: function (scope, iElement, attrs) {
            scope.$on("enrichment.results", function () {
                queryBuilder.addFilter(scope.filter.name, angular.isString(scope.filterText)
                    ? sanitize.stripTags(scope.filterText)
                    : null);
            });
        }
    }
}]);

app.directive("boolFilter",
          ["queryBuilder", function (queryBuilder) {
    return {
        restrict: "E",
        templateUrl: "partials/bool-filter.html",
        controller: function ($scope, $element, $attrs, $transclude) {},
        scope: {
            filter: "="
        },
        link: function (scope, iElement, attrs) {
            scope.$on("enrichment.results", function () {
                var v = scope.choice;
                queryBuilder.addFilter(scope.filter.name, !!v);
            });
        }
    }
}]);



})(angular);