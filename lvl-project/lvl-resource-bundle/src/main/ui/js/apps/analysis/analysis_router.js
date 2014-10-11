/**
 * RequireJS module that defines the routes of the sub-application: analysis.
 */

define([ 'app', 'apps/config/marionette/configuration', 'routefilter' ], function(Lvl, Configuration) {
    Lvl.module('Routers.AnalysisApp', function(AnalysisAppRouter, Lvl, Backbone, Marionette, $, _) {
        'use strict';
        var config = new Configuration();
        var Router = Backbone.Router.extend({
            routes : {
                'analysis' : 'defaultAnalysis',
                'analysis/pipelines' : 'showPipelines',
                'analysis/datasets' : 'showDatasets',
                'analysis/runs' : 'showRuns'
            },
            before : function() {
                if (!config.isAuthenticated()) {
                    Lvl.navigate('login/' + encodeURIComponent(Backbone.history.fragment) + '/unauthenticated', {
                        trigger : true,
                        replace : true
                    });
                    return false;
                }
                require([ 'apps/analysis/analysis_app' ], function() {
                    Lvl.execute('set:active:header', 'workspace', 'analysis');
                    Lvl.execute('set:active:footer', 'workspace');
                    Lvl.startSubApp('AnalysisApp');
                });
                return true;
            },
            defaultAnalysis : function() {
                var self = this;
                Lvl.navigate('analysis/pipelines', {
                    trigger : true,
                    replace : true
                });
            },
            showPipelines : function() {
                Lvl.execute('analysis:set:active', 'pipelines');
            },
            showDatasets : function() {
                Lvl.execute('analysis:set:active', 'datasets');
            },
            showRuns : function() {
                Lvl.execute('analysis:set:active', 'runs');
            }
        });
        Lvl.addInitializer(function() {
            var router = new Router();
        });
    });
});