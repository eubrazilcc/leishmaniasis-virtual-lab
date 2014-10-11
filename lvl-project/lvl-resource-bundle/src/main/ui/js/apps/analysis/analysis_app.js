/**
 * RequireJS module that defines the sub-application: analysis.
 */

define([ 'app' ], function(Lvl) {
    Lvl.module('AnalysisApp', function(AnalysisApp, Lvl, Backbone, Marionette, $, _) {
        'use strict';

        /* Initialization & finalization */
        AnalysisApp.startWithParent = false;

        AnalysisApp.onStart = function() {
            console.log('starting AnalysisApp');
        };

        AnalysisApp.onStop = function() {
            console.log('stopping AnalysisApp');
        };

        AnalysisApp.currentSection = null;

        /* Commands and events */
        Lvl.commands.setHandler('analysis:set:active', function(section) {
            section = section || 'default';
            if (AnalysisApp.currentSection !== section) {
                if (section === 'pipelines' || section === 'datasets' || section === 'runs') {
                    require([ 'apps/analysis/layout/analysis_layout_ctrl' ], function(LayoutController) {
                        AnalysisApp.currentSection = LayoutController.showLayout(section);
                    });
                } else {
                    Lvl.mainRegion.currentView.reset();
                    AnalysisApp.currentSection = null;
                }
            }
        });
    });
});