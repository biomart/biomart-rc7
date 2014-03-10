;(function (angular) {
"use strict";

angular.module("martVisual.services").

service("bmservice",
        ["$http", "mvConfig",
        function bmservice ($http, config) {

    var url = config.url, queryUrl = config.queryUrl,
        baseOpts = {
            cache: true,
            method: "GET",
            timeout: 1e6
        };

    //
    // It gets the marts coming from the guicontainer guiContainer.
    // guiContainer is expected to be a string representing the name of the
    // guicontainer.
    //
    // e.g. gui.json?name=Enrichement
    this.marts = function marts(guiContainer) {
        return $http.get(url + "/gui.json?name="+guiContainer, baseOpts);
    };


    // datasets/mapped.json\?mart\=gene_ensembl_config_3_1_2
    this.datasets = function datasets(mart) {
        var iUrl = url + "/datasets/mapped.json?mart=" + mart
        return $http.get(iUrl, baseOpts);
    };


    // containers.json?datasets=hsapiens_gene_ensembl&withfilters=true&withattributes=false&config=gene_ensembl_config_3_1_2
    this.containers = function containers (datasets, config, withFilters, withAttributes) {
        var fs = withFilters, as = withAttributes,
            iUrl = url + "/containers.json?datasets="+datasets+"&config="+ config;

        if (angular.isDefined(fs)) iUrl += "&withfilters=" + !!fs;
        if (angular.isDefined(as)) iUrl += "&withattributes=" + !!as;
        return $http.get(iUrl, baseOpts);
    };

    this.query = function (xml) {
        var opt = angular.extend({
            params: { query: xml },
            // data: "query="+xml,
            headers: {
                "Content-Type": "application/xml"
            }
        }, baseOpts);
        return $http.get(queryUrl, opt);
    }



}]);

})(angular);