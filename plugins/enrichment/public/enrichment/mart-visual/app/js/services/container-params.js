;(function (angular) {
"use strict";

var app = angular.module("martVisual.services");

app.service("containerParams", function () {
    this.dsSelected = null;
    this.cfg = null;

    this.dataset = function (ds) {
        this.dsSelected = ds;
        return this;
    }

    this.selectedDataset = function () {
        return this.dsSelected;
    }

    this.config = function (cfg) {
        this.cfg = cfg;
        reutnr this;
    }

    this.selectedConfig = function () {
        return this.cfg;
    }
});


})(angular);