;(function (angular) {
"use strict";

var app = angular.module("martVisual.services");

app.factory("sanitize", function () {

    // From prototype.js!
    return {
        stripTags: function stripTags(str) {
            return str.replace(/<\w+(\s+("[^"]*"|'[^']*'|[^>])+)?>|<\/\w+>/gi, '');
        }
    };

});

})(angular);