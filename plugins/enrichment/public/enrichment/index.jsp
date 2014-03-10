<!doctype html>
<%@ page language="java" %>
<%@ page session="false" %>
<%@ page contentType="text/html;charset=UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/bmtaglib.tld" prefix="bm" %>
<html lang="en">
<c:set var="currentPage" scope="request" value="Enrichment"/>
<head>
  <c:import url="/conf/config.jsp" context="/"/>
    <title>${labels.document_title}</title>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <link rel="stylesheet" type="text/css" href="mart-visual/app/css/bootstrap/bootstrap.mod.css">
    <link rel="stylesheet" href="mart-visual/app/css/app.css"/></head>
    <link rel="stylesheet" href="mart-visual-enrichment/app/css/app.css"/></head>
</head>
<body>
    <div  ng-app="compApp" >
        <div ng-view></div>
    </div>


     <script type="text/ng-template" id="martvisual.html">
        <div class="container" ng-controller="MartVisualCtrl">
            <div class="row">
                <div class="col-md-3 dataset-section">
                    <h2>Datasets Available</h2>
                    <select class="form-control mv-select" ng-model="selectedDataset" ng-options="d.displayName for d in datasets" ng-change="datasetChanged()"></select>
               </div>
               <div class="col-md-9 dataset-info">
                    <a href="" class="btn btn-danger" ng-click="getResults()">Results</a>
               </div>
            </div>
            <div class="row" ng-controller="ContainerCtrl">
                <div class="col-md-9">
                    <containerset containers="containers"></container>
                </div>
                <div class="col-md-3"></div>
            </div>
        </div>
     </script>


    <script type="text/ng-template" id="bool-filter.html">
    <div>
<h3>{{filter.displayName}}</h3>
<p>{{filter.description}}</p>
<input type="radio" ng-model="choice" value="true"> Yes
<input type="radio" ng-model="choice" value="false"> No
</div>
    </script>

    <script type="text/ng-template" id="container.html">
        <div collapse="container.isCollapsed">
        </div>
    </script>


    <script type="text/ng-template" id="bio-attribute.html">
    <div><input type="checkbox" ng-model="value" ng-true-value="{{bio.name}}" ng-false-value="{{null}}"> {{bio.displayName}}</div>
    </script>


    <script type="text/ng-template" id="containerset.html">        <div class="row">
<div class="row">
    <div class="col-md-9">
        <container ng-repeat="c in flatContainers" container="c" ng-if="(!!c.filters.length) || (!!c.attributes.length)"></container>
    </div>
    <div class="col-md-3">
        <ul class="nav">

        </ul>
    </div>
</div>
    </script>


    <script type="text/ng-template" id="range.html">
<input type="range" min="minRange" max="maxRange" value="currentValue" step="step" />
<span>{{currentValue}}</span>
    </script>

    <script type="text/ng-template" id="single-select-upload-filter.html">
    <div>
        <div>
            <h3>{{filter.displayName}}</h3>
            <p>{{filter.description}}</p>
        <div>
        <select class="form-control mv-select" ng-model="selected" ng-options="f.displayName for f in options"></select>
        <textarea class="form-control mv-tarea" rows="8" cols="60" ng-model="textareaValue"></textarea>
        <input type="file" accept="text/*">
        <label for="fileupload">Upload local file</label>
    </div>
    </script>

        <script type="text/ng-template" id="upload-filter.html">
        <div>
            <div>
                <h3>{{filter.displayName}}</h3>
                <p>{{filter.description}}</p>
            <div>
            <textarea class="form-control mv-tarea" rows="8" cols="60" ng-model="textareaValue"></textarea>
            <input type="file" accept="text/*">
            <label for="fileupload">Upload local file</label>
        </div>
    </script>

    <script type="text/ng-template" id="table-of-results.html">
<table class="table table-condensed table-hover">
    <thead>
        <tr>
            <th>Description</th>
            <th>P-Value</th>
            <th>Corrected P-Value</th>
        </tr>
    </thead>
    <tbody>
        <tr ng-repeat="term in results">
            <td>{{term.description}}</td>
            <td>{{term.pvalue}}</td>
            <td>{{term.bpvalue}}</td>
        </tr>
    </tbody>
</table>

    </script>

    <script type="text/ng-template" id="text-filter.html">        <div>
    <h3>{{filter.displayName}}</h3>
<p>{{filter.description}}</p>
<input type="text" ng-model="filterText">
</div>
    </script>


    <script type="text/ng-template" id="vis.html">
<div class="mv-vis-container"></div>
    </script>

    <script type="text/ng-template" id="mart-visual-enrichment.html">
     <div class="container-fluid" ng-controller="MartVisualEnrichmentCtrl">
        <loading></loading>
        <tabset>
            <tab ng-repeat="mvTab in mvTabs">
              <tab-heading><a href="">{{mvTab.title}}</a></tab-heading>
                <div class="row">
                    <div class="col-md-3 left-panel" >
                        <div ng-controller="ResultsTableCtrl">
                            <div>
                                <input class="form-control" type="text" ng-model="pattern"
                                       ng-change="filterByDescription(pattern)"
                                       ng-trim="false"
                                       placeholder="Search by description...">
                            </div>
                            <hr />
                            <div class="mv-table-container">
                                <mv-results-table></mv-results-table>
                            </div>
                        </div>
                        <hr />

                    </div>

                    <div class="col-md-9 right-panel" ng-controller="GraphCtrl">
                        <mv-graph nodes="nodes" edges="edges" filter-pattern="search"></mv-graph>
                    </div>
                </div>
            </tab>
        </tabset>
    </div>
    </script>


<script src="mart-visual/app/lib/angular/angular.js"></script>
<script src="mart-visual/app/lib/angular/angular-route.js"></script>
<script src="mart-visual/app/lib/jquery.js"></script>
<script type="text/javascript" src="mart-visual/app/lib/ui-bootstrap-tpls-0.10.0.js"></script>
<script type="text/javascript" src="mart-visual/app/lib/cytoscape.js"></script>

<script src="mart-visual/app/js/app.js"></script>
<script src="mart-visual/app/js/services.js"></script>
<script src="mart-visual/app/js/services/mv-config.js"></script>
<script src="mart-visual/app/js/services/bmservice.js"></script>
<script src="mart-visual/app/js/services/query-builder.js"></script>
<script src="mart-visual/app/js/services/sanitize.js"></script>
<script src="mart-visual/app/js/controllers.js"></script>
<script src="mart-visual/app/js/controllers/mart-visual.js"></script>
<script src="mart-visual/app/js/controllers/container.js"></script>
<script src="mart-visual/app/js/directives.js"></script>
<script src="mart-visual/app/js/directives/filters.js"></script>
<script src="mart-visual/app/js/directives/containers.js"></script>

<script src="mart-visual-enrichment/app/js/app.js"></script>
<script src="mart-visual-enrichment/app/lib/cytoscape.js"></script>
<script src="mart-visual-enrichment/app/js/services.js"></script>
<script src="mart-visual-enrichment/app/js/services/tabs.js"></script>
<script src="mart-visual-enrichment/app/js/services/terms-async.js"></script>
<script src="mart-visual-enrichment/app/js/services/terms.js"></script>
<script src="mart-visual-enrichment/app/js/services/data-retrieve.js"></script>
<script src="mart-visual-enrichment/app/js/controllers.js"></script>
<script src="mart-visual-enrichment/app/js/directives.js"></script>
<script src="mart-visual-enrichment/app/js/directives/mv-graph.js"></script>
<script src="mart-visual-enrichment/app/js/directives/mv-results-table.js"></script>
<script src="mart-visual-enrichment/app/js/directives/loading.js"></script>

<script src="compapp.js"></script>

</body>
</html>