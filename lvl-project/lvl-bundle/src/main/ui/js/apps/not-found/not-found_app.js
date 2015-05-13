/**
 * RequireJS module that defines the sub-application: not-found.
 */

define([ 'app' ], function(Lvl) {
	Lvl.module('NotFoundApp', function(NotFoundApp, Lvl, Backbone, Marionette, $, _) {
		'use strict';

		/* Initialization & finalization */
		NotFoundApp.startWithParent = false;

		NotFoundApp.onStart = function() {
			console.log('starting NotFoundApp');
		};

		NotFoundApp.onStop = function() {
			console.log('stopping NotFoundApp');
		};

		/* Commands and events */
		Lvl.commands.setHandler('show:not_found', function() {
			require([ 'apps/not-found/show/not-found_show_ctrl' ], function(ShowController) {
				ShowController.showNotFound();
			});
		});
	});
});