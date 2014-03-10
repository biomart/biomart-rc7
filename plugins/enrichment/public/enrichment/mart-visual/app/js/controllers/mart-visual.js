;(function (angular) {
"use strict";

angular.module("martVisual.controllers").
controller("MartVisualCtrl",
           ["$scope", "$log", "$location", "bmservice", "queryBuilder",
           function MartVisualCtrl($scope, $log, $location, bmservice, queryBuilder) {

    var gui = $location.search().gui, mart = $location.search().mart, martIntName;
    // marts of this gui container
    bmservice.marts(gui).then(function (res) {
        // data should be json
        var data = res.data;
        martIntName = data.marts[0].name;
        $scope.config = data.marts[0].config;
        // ctrl.selectConfig($scope.config);
        return bmservice.datasets(martIntName);
    }).then(function (res) {
        var data = res.data;
        $scope.datasets = data[martIntName];
        $scope.selectedDataset = $scope.datasets[0];
        // ctrl.selectDataset($scope.selectedDataset);
        return bmservice.containers($scope.selectedDataset.name, $scope.config, true);
    }).then(function (res) {
        $scope.containers = res.data.containers;
    }).catch(function (reason) {
        $log.error("MartVisual controller: "+reason);
    });


    $scope.datasetChanged = function () {
        bmservice.containers($scope.selectedDataset.name, $scope.config, true)
        .then(function (res) {
            $scope.containers = res.data.containers;
        });
    }

    $scope.getResults = function () {
        $scope.$broadcast("enrichment.results");
        setTimeout(function () {
            $scope.$apply(function () {
                var xml = queryBuilder.build($scope.selectedDataset.name, $scope.config);
                $location.path($location.path() + "results");
            });
        }, 10);
    }

}]);

})(angular);