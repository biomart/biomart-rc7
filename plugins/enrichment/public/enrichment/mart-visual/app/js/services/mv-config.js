;(function (angular) {
"use strict";

angular.module("martVisual.services").

factory("mvConfig",
         [function mvConfig() {

    var configElemId = "config", elem = document.getElementById(configElemId),
        config = { url: "/martservice", queryUrl: "/martservice/results" };

    if (elem) {
        config = JSON.parse(elem.textContent);
    }

    return config;

}]);


})(angular);