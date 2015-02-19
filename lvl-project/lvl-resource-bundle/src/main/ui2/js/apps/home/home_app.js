/**
 * RequireJS module that defines the sub-application: home.
 */

define([ 'app' ], function(Lvl) {
	Lvl.module('HomeApp', function(HomeApp, Lvl, Backbone, Marionette, $, _) {
		'use strict';

		/* Initialization & finalization */
		HomeApp.startWithParent = false;

		HomeApp.onStart = function() {
			console.log('starting HomeApp');
		};

		HomeApp.onStop = function() {
			console.log('stopping HomeApp');
		};

		/* Commands and events */
		Lvl.commands.setHandler('show:home', function() {
			require([ 'apps/home/show/home_show_ctrl' ], function(ShowController) {
				ShowController.showHome();
			});
		});
	});
});