describe("biomart filters", function () {
    var expect = chai.expect, $compile, $rootScope, $scope, filter, queryBuilder;

    beforeEach(module("martVisualEnrichment.directives"));
    beforeEach(module(function ($provide) {
        queryBuilder = {
            q: {},
            addFilter: function (name, value) {
                this.q[name] = value;
            }
        };

        $provide.value("queryBuilder", queryBuilder);
    }));
    beforeEach(inject(function (_$compile_, _$rootScope_, $templateCache) {
        // console.log(window.__html__)
        $templateCache.put('partials/upload-filter.html', window.__html__['app/partials/upload-filter.html']);
        $templateCache.put('partials/single-select-upload-filter.html', window.__html__['app/partials/single-select-upload-filter.html']);
        $compile = _$compile_;
        $rootScope = _$rootScope_;
        $scope = $rootScope.$new();
        filter = {"name":"hgnc_symbol_3","displayName":"HGNC symbol(s) [e.g. ZFY]","description":"","type":"upload","isHidden":true,"qualifier":"","required":false,"attribute":"hgnc_symbol","filters":[],"values":[],"parent":"gene__filters","dependsOn":""};
        filter.filters = [{"name":"ensembl_exon_id_3","displayName":"Ensembl exon ID(s) [e.g. ENSE00001508081]","description":"","type":"upload","isHidden":true,"qualifier":"","required":false,"attribute":"ensembl_peptide_id","filters":[],"values":[],"parent":"gene__filters","dependsOn":""},{"name":"hgnc_id_3","displayName":"HGNC ID(s) [e.g. 43668]","description":"","type":"upload","isHidden":true,"qualifier":"","required":false,"attribute":"hgnc_id","filters":[],"values":[],"parent":"gene__filters","dependsOn":""}];
    }));

    describe("uploadFilter", function () {
        describe ("adds itself to the queryBuilder on enrichment.results event", function () {
            it ("with name the name of the filter and value empty if user didn't inserted", function () {
                $scope.filter = filter;
                var elem = $compile('<upload-filter filter="filter"></upload-filter>')($scope);
                $rootScope.$digest();
                $scope.$broadcast("enrichment.results");
                expect(elem.scope()).to.be.ok;
                expect(queryBuilder.q[filter.name]).to.be.equal("");
            });

            it ("with name the name of the filter and as value the textarea content", function () {
                $scope.filter = filter;
                var elem = $compile('<upload-filter filter="filter"></upload-filter>')($scope);
                $rootScope.$digest();
                elem.find("textarea").text("fooo");
                $scope.$broadcast("enrichment.results");
                expect(queryBuilder.q[filter.name]).to.be.equal("fooo");
            });
        });
    });

    describe("singleSelectUploadFilter", function () {
        it ("registers itself to the querybuilder with the option selected and the text of textarea", function () {
            $scope.filter = filter;
            var elem = $compile('<single-select-upload-filter filter="filter"></single-select-upload-filter>')($scope);
            $rootScope.$digest();
            elem.find("textarea").text("fooo");
            $scope.$broadcast("enrichment.results");
            expect(queryBuilder.q).to.have.property(filter.filters[0].name, "fooo");
            // $scope.$broadcast("enrichment.results");
            // elem.find("option").eq(1)[0].click();
            // elem.find("select").val(filter.filters[1].displayName);
            // expect(queryBuilder.q).to.have.property(filter.filters[0].name, "fooo");
        });
    });
});