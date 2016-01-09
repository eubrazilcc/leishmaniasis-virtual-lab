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
		Lvl.commands.setHandler('maps:show', function() {
			require([ 'apps/maps/show/maps_show_ctrl' ], function(MapsController) {
				MapsController.showMaps();
			});
		});
	});
});