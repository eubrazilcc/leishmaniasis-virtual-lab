/**
 * RequireJS module that defines the sub-application: settings.
 */

define([ 'app' ], function(Lvl) {
	Lvl.module('SettingsApp', function(SettingsApp, Lvl, Backbone, Marionette, $, _) {
		'use strict';

		/* Initialization & finalization */
		SettingsApp.startWithParent = false;

		SettingsApp.onStart = function() {
			console.log('starting SettingsApp');
		};

		SettingsApp.onStop = function() {
			console.log('stopping SettingsApp');
		};

		/* Commands and events */
		Lvl.commands.setHandler('settings:set:active', function(section) {
			require([ 'apps/settings/layout/settings_layout_ctrl' ], function(LayoutController) {
				SettingsApp.currentSection = LayoutController.showLayout(section);
			});
		});
	});
});