/**
 * RequireJS module that defines the sub-application: open->software.
 */

define([ 'app' ], function(Lvl) {
	Lvl.module('SoftwareApp', function(SoftwareApp, Lvl, Backbone, Marionette, $, _) {
		'use strict';

		/* Initialization & finalization */
		SoftwareApp.startWithParent = false;

		SoftwareApp.onStart = function() {
			console.log('starting SoftwareApp');
		};

		SoftwareApp.onStop = function() {
			console.log('stopping SoftwareApp');
		};

		/* Commands and events */
		Lvl.commands.setHandler('show:software', function(section) {
			require([ 'apps/open/software/show/software_show_ctrl' ], function(ShowController) {
				ShowController.showSoftware(section);
			});
		});
	});
});