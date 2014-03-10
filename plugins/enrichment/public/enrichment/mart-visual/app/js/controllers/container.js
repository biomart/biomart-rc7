;(function (angular) {
"use strict";

angular.module("martVisual.controllers").
controller("ContainerCtrl",
           ["$scope", "$log", "bmservice",
           function ContainerCtrl($scope, $log, bmservice) {

    this.contEls = [];
    this.addContainer = function (container) {
        this.contEls.push(container);
    }

    // this.toggle = function (container) {
    //     this.contEls.forEach(function (c) {
    //         if (c === container) {
    //             c.isCollapsed = !c.isCollapsed;
    //         }
    //     })
    // }

    this.toggle = function (name) {
        this.contEls.forEach(function (c) {
            if (c.name === name) {
                $scope.$apply(function () {
                    c.isCollapsed = !c.isCollapsed;
                })
            }
        })
    }

}]);

})(angular);