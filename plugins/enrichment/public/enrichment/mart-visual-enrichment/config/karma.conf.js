module.exports = function(config){
    config.set({
    basePath : "../",

    files : [
        "app/lib/angular/angular.js",
        "app/lib/angular/angular-*.js",
        "app/lib/jquery.js",
        "test/lib/angular/angular-mocks.js",
        "test/lib/chai.js",
        "app/js/**/*.js",
        "test/unit/services/**/*.js",
        "test/unit/controllers/**/*.js",
        "test/unit/directives/**/*.js",
        "app/partials/*.html"
    ],

    exclude : [
        "app/lib/angular/*.min.js",
        "app/lib/angular/angular-scenario.js"
    ],

    autoWatch : true,

    frameworks: ["mocha"],

    browsers : ["Chrome"],

    plugins : [
        "karma-junit-reporter",
        "karma-chrome-launcher",
        "karma-firefox-launcher",
        "karma-safari-launcher",
        "karma-jasmine",
        "karma-mocha-reporter",
        "karma-mocha",
        "karma-html2js-preprocessor"
    ],

    reporters: ["progress"],

    junitReporter : {
        outputFile: "test_out/unit.xml",
        suite: "unit"
    },

    preprocessors: {
        "app/partials/*.html": ["html2js"]
    }

})}
