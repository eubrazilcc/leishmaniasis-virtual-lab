/**
 * RequireJS module that defines the sub-application: enm.
 */

define([ 'app' ], function(Lvl) {
	Lvl.module('EnmApp', function(EnmApp, Lvl, Backbone, Marionette, $, _) {
		'use strict';

		/* Initialization & finalization */
		EnmApp.startWithParent = false;

		EnmApp.onStart = function() {
			console.log('starting EnmApp');
		};

		EnmApp.onStop = function() {
			console.log('stopping EnmApp');
		};

		/* Commands and events */
		Lvl.commands.setHandler('show:enm', function() {
			require([ 'apps/enm/show/enm_show_ctrl' ], function(ShowController) {
				ShowController.showEnm();
			});
		});
	});
});