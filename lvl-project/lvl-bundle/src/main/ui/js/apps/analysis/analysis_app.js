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

		/* Commands and events */
		Lvl.commands.setHandler('analysis:set:active', function(section, subsection) {
			require([ 'apps/analysis/layout/analysis_layout_ctrl' ], function(LayoutController) {
				AnalysisApp.currentSection = LayoutController.showLayout(section, subsection);
			});
		});
	});
});