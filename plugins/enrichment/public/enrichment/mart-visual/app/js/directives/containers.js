;(function (angular) {
"use strict";

var app = angular.module("martVisual.directives");

app.directive("bioAttribute", [
    "queryBuilder",
    function (queryBuilder) {
        return {
            restrict: "E",
            scope: {
                bio: "="
            },
            replace: true,
            templateUrl: "partials/bio-attribute.html",
            link: function (scope, iElement, iAttrs) {
                var added = false;
                scope.$on("enrichment.results", function (evt) {
                    if (!added && scope.value) {
                        queryBuilder.addAttribute(scope.value);
                    }
                })
            }
        }
    }
]);


function filterTemplate(type, i) {
    switch(type) {
        case "boolean":
            return '<bool-filter filter="filters['+i+']"></bool-filter></hr>';
        case "text":
            return '<text-filter filter="filters['+i+']"></text-filter></hr>';
        case "upload":
            return '<upload-filter filter="filters['+i+']"></upload-filter></hr>';
        case "singleSelectUpload":
            return '<single-select-upload-filter filter="filters['+i+']"></single-select-upload-filter></hr>';
    }
}


app.directive("container",
              ["$compile",
              function container($compile) {

    return {
        restrict: "E",
        replace: true,
        scope: {
            container: "="
        },
        require: "^containerset",
        // controller: "ContainerCtrl",
        templateUrl: "partials/container.html",
        link: function (scope, iElement, iAttrs, ctrl) {
            ctrl.addContainer(scope.container);
            scope.container.isCollapsed = true;
            scope.attributes = scope.container.attributes;
            scope.filters = scope.container.filters;
            // expect angular element
            var fls = [], ats = [];
            scope.filters.forEach(function (fl, i) {
                fls.push(filterTemplate(fl.type, i));
            });
            scope.attributes.forEach(function (a, i) {
                ats.push('<bio-attribute bio="attributes['+i+']"></bio-attribute>');
            })
            var well = angular.element('<div class="well well-lg mv-container"><h2>{{container.displayName}}</h2></div>')
            if (fls.length) well.append(fls.join("\n"));
            if (ats.length) well.append(ats.join("\n"));
            iElement.append(well);
            $compile(iElement.contents())(scope);
        }
    }
}]);

app.directive("containerNavElem",
              [function containerNavElem() {

    return {
        require: "^containerset",
        restrict: "E",
        template: '<li><a href="">{{displayName}}</a></li>',
        scope: {
            name: "@",
            displayName: "@"
        },
        link: function (scope, iElement, iAttrs, ctrl) {
            iElement.on("click", function (evt) {
                ctrl.toggle(scope.name);
            });
        }
    }

}]);

function mkPoint(rootElem, cont) {
    return angular.element('<container-nav-elem name="'+cont.name+'" display-name="'+cont.displayName+'"/>');
}

function buildNav(rootElem, cont) {
    cont.forEach(function (c) {
        buildNavHelper(rootElem, c);
    });
}

function buildNavHelper(rootElem, cont) {
    rootElem.append(mkPoint(rootElem, cont));
    if (cont.containers.length > 0) {
        var ul = angular.element("<ul>");
        cont.containers.forEach(function (c) {
            buildNavHelper(ul, c);
        });
        rootElem.append(ul);
    }
}

function flatten(containers, coll) {
    containers.forEach(function (c) {
        flattenCont(c, coll);
    })
}

function flattenCont(root, coll) {
    coll.push(root);
    if (root.containers.length > 0) {
        flatten(root.containers, coll);
    }
}


app.directive("containerset",
              ["$compile",
              function containerset($compile) {

    return {
        controller: "ContainerCtrl",
        restrict: "E",
        replace: true,
        templateUrl: "partials/containerset.html",
        scope: {
            containers: "="
        },
        link: function (scope, iElement, iAttrs, ctrl) {
            var initFlag = false;
            scope.$watch('containers', function (newCont) {
                if (newCont) {
                    if (initFlag) {
                        rmContainers();
                    }
                    mkContainers();
                    initFlag = true;
                }
            });

            function rmContainers() {
                iElement.find("ul").contents().remove();
                // iElement.find(".col-md-9").contents().remove();
            }

            function mkContainers() {
                scope.flatContainers = [];
                flatten(scope.containers, scope.flatContainers);
                var nav = iElement.find("ul");
                buildNav(nav, scope.containers);
                $compile(nav.contents())(scope);
            }
        }
    }

}]);


})(angular);