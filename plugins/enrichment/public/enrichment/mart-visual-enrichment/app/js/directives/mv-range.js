;(function (angular) {
"use strict";

angular.module("martVisualEnrichment.directives").

directive("mvRange", function () {
    return {
        restrict: "E",
        templateUrl: "partials/range.html",
        scope: {
            minRange: "=",
            maxRange: "=",
            value: "=",
            step: "=",
            currentValue: "="
        },
        link: function (scope, iElement, iAttrs) {
            iElement.on("click", scope.clickHandler);
            scope.on("$destroy", function () {
                iElement.off(scope.clickHandler);
            });
            $scope.clickHandler = function (evt) {
                this.currentValue = evt.target.value;
            }
        }
    }
})


})(angular);