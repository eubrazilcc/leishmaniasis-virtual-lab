/**
 * RequireJS module that defines the sub-application: support.
 */

define([ 'app' ], function(Lvl) {
	Lvl.module('SupportApp', function(SupportApp, Lvl, Backbone, Marionette, $, _) {
		'use strict';

		/* Initialization & finalization */
		SupportApp.startWithParent = false;

		SupportApp.onStart = function() {
			console.log('starting SupportApp');
		};

		SupportApp.onStop = function() {
			console.log('stopping SupportApp');
		};

		/* Commands and events */
		Lvl.commands.setHandler('show:support', function(section) {
			require([ 'apps/support/show/support_show_ctrl' ], function(ShowController) {
				ShowController.showSupport(section);
			});
		});
	});
});