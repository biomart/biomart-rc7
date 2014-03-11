;(function (angular) {

"use strict";

angular.module("martVisualEnrichment.services").

factory("termsSync", function () {
    function termsSync (nodes) {
        this._all = nodes.filter(function filterTerms(node) {
            return node.type === "term";
        });

        this._filtered = this._all;
    }


    /**
     * Remove all the filters and returns a promise that resolves to all results.
     * This is the apparent behaviour, not what really will happen.
     **/
    termsSync.prototype.all = function () {
        this._clearFilters();
        return this._filtered;
    }


    termsSync.prototype.clear = function () {
        this._clearFilters();
        return this;
    }


    termsSync.prototype._clearFilters = function () {
        this._filtered = null;
        this._filtered = this._all;
        return this;
    }


    termsSync.prototype.filterByDescription = function (desc) {
        if (desc && desc !== "") {
            var pattern = new RegExp(desc, "i");
            // var strip = /\W/g, pattern = new RegExp(desc.replace(strip, ""), "i");
            this._filtered = this._filtered.filter(function (term) {
                return term.description.match(pattern);
            });
        }
        return this._filtered;
    }


    termsSync.prototype.filterByScore = function (min, max) {
        this._filtered = this._filtered.filter(function (term) {
            return term["p-value"] >= min && term["p-value"] < max;
        });
        return this._filtered;
    }


    return termsSync;
});

})(angular);