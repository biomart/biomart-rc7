;(function (angular) {

"use strict";

angular.module("martVisualEnrichment.controllers", []).

controller("MartVisualEnrichmentCtrl",
           ["$scope", "tabs",
           function MartVisualCtrl ($scope, tabs) {

    $scope.mvTabs = [];

    tabs.then(function (tabs) {
        $scope.mvTabs = tabs;
    });

}]).

controller("ResultsTableCtrl",
           ["$scope", "$q", "$rootScope", "terms",
           function ResultsTableCtrl ($scope, $q, $rootScope, terms) {

    var init = true;
    $scope.terms = terms.get($q.when($scope.mvTab.nodes));
    $rootScope.$emit("enrichment.dataloading");
    $scope.terms.all().then(function (ts) {
        if (init) {
            $rootScope.$emit("enrichment.dataloaded");
            init = false;
        }
        $scope.results = ts;
    });

    $scope.filterByDescription = function (desc) {
        $scope.mvTab.pattern = desc.toLowerCase();
        // Clean filters
        $scope.terms.all().filterByDescription(desc).then(function (ts) {
            $scope.results = ts;
        });
    };
}]).

controller("GraphCtrl",
           ["$scope", "$timeout",
           function ($scope, $timeout) {
    var timeout = null
    $scope.nodes = $scope.mvTab.nodes;
    $scope.edges = $scope.mvTab.edges;

    $scope.$watch(function (scope) {
        return scope.mvTab.pattern;
    }, function () {
        if (timeout) {
            $timeout.cancel(timeout);
        }
        timeout = $timeout(function () {
            $scope.search = $scope.mvTab.pattern;
        }, 500);
    });
}]);


})(angular);
