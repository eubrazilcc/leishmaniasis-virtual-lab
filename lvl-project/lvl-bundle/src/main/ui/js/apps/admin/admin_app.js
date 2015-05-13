/**
 * RequireJS module that defines the sub-application: admin.
 */

define([ 'app' ], function(Lvl) {
	Lvl.module('AdminApp', function(AdminApp, Lvl, Backbone, Marionette, $, _) {
		'use strict';

		/* Initialization & finalization */
		AdminApp.startWithParent = false;

		AdminApp.onStart = function() {
			console.log('starting AdminApp');
		};

		AdminApp.onStop = function() {
			console.log('stopping AdminApp');
		};

		/* Commands and events */
		Lvl.commands.setHandler('admin:set:active', function(section) {
			require([ 'apps/admin/layout/admin_layout_ctrl' ], function(LayoutController) {
				AdminApp.currentSection = LayoutController.showLayout(section);
			});
		});
	});
});