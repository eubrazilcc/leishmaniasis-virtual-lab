/**
 * RequireJS module that defines the sub-application: maps.
 */

define([ 'app' ], function(Lvl) {
	Lvl.module('MapsApp', function(MapsApp, Lvl, Backbone, Marionette, $, _) {
		'use strict';

		/* Initialization & finalization */
		MapsApp.startWithParent = false;

		MapsApp.onStart = function() {
			console.log('starting MapsApp');
		};

		MapsApp.onStop = function() {
			console.log('stopping MapsApp');
		};

		/* Commands and events */
		Lvl.commands.setHandler('maps:set:active', function(section) {
			require([ 'apps/maps/layout/maps_layout_ctrl' ], function(LayoutController) {
				MapsApp.currentSection = LayoutController.showLayout(section);
			});
		});
	});
});