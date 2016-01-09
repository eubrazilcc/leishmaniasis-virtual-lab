/**
 * RequireJS module that defines the sub-application: curation.
 */

define([ 'app' ], function(Lvl) {
	Lvl.module('CurationApp', function(CurationApp, Lvl, Backbone, Marionette, $, _) {
		'use strict';

		/* Initialization & finalization */
		CurationApp.startWithParent = false;

		CurationApp.onStart = function() {
			console.log('starting CurationApp');
		};

		CurationApp.onStop = function() {
			console.log('stopping CurationApp');
		};

		/* Commands and events */
		Lvl.commands.setHandler('curation:set:active', function(section) {
			require([ 'apps/curation/layout/curation_layout_ctrl' ], function(LayoutController) {
				CurationApp.currentSection = LayoutController.showLayout(section);
			});
		});
	});
});