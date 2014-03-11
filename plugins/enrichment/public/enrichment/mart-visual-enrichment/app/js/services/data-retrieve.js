;(function (angular) {

"use strict";

angular.module("martVisualEnrichment.services").
service("dataRetrieve",
        ["$log", "bmservice", "queryBuilder", "$rootScope",
        function dataRetrieveService ($log, bmservice, queryBuilder, $rootScope) {

    this.then = function (th, ch, nt) {
        var xml = queryBuilder.getXml(), p;
        p = bmservice.query(xml).then(function then (res) {
            return res.data;
        }, function rejected (res) {
            $log.error("The results request went wrong: ", res.status);
            return res;
        }).then(th, ch, nt);
        // $rootScope.$apply();
        return p;
    }
}]);

})(angular);