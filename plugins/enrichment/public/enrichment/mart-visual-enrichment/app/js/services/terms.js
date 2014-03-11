;(function (angular) {

"use strict";

angular.module("martVisualEnrichment.services").
service("terms",
        ["termsAsync",
        function termsService(termsAsync) {
    this.get = function model (nodes) {
        return new termsAsync(nodes);
    };
}]);

})(angular);