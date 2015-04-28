/**
 * RequireJS module that defines the sub-application: open->about.
 */

define([ 'app' ], function(Lvl) {
	Lvl.module('AboutApp', function(AboutApp, Lvl, Backbone, Marionette, $, _) {
		'use strict';

		/* Initialization & finalization */
		AboutApp.startWithParent = false;

		AboutApp.onStart = function() {
			console.log('starting AboutApp');
		};

		AboutApp.onStop = function() {
			console.log('stopping AboutApp');
		};

		/* Commands and events */
		Lvl.commands.setHandler('show:about', function(section) {
			require([ 'apps/open/about/show/about_show_ctrl' ], function(ShowController) {
				ShowController.showAbout(section);
			});
		});
	});
});