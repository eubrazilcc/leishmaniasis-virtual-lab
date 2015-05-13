/**
 * RequireJS module that defines the sub-application: drive.
 */

define([ 'app' ], function(Lvl) {
	Lvl.module('DriveApp', function(DriveApp, Lvl, Backbone, Marionette, $, _) {
		'use strict';

		/* Initialization & finalization */
		DriveApp.startWithParent = false;

		DriveApp.onStart = function() {
			console.log('starting DriveApp');
		};

		DriveApp.onStop = function() {
			console.log('stopping DriveApp');
		};

		/* Commands and events */
		Lvl.commands.setHandler('drive:set:active', function(section) {
			require([ 'apps/drive/layout/drive_layout_ctrl' ], function(LayoutController) {
				DriveApp.currentSection = LayoutController.showLayout(section);
			});
		});
	});
});