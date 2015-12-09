/**
 * RequireJS module that defines the sub-application: statistics.
 */

define([ 'app' ], function(Lvl) {
	Lvl.module('StatisticsApp', function(StatisticsApp, Lvl, Backbone, Marionette, $, _) {
		'use strict';

		/* Initialization & finalization */
		StatisticsApp.startWithParent = false;

		StatisticsApp.onStart = function() {
			console.log('starting StatisticsApp');
		};

		StatisticsApp.onStop = function() {
			console.log('stopping StatisticsApp');
		};

		/* Commands and events */
		Lvl.commands.setHandler('statistics:set:active', function(section) {
			require([ 'apps/statistics/layout/statistics_layout_ctrl' ], function(LayoutController) {
				StatisticsApp.currentSection = LayoutController.showLayout(section);
			});
		});
	});
});