/**
 * RequireJS module that defines the sub-application: saved-items.
 */

define([ 'app' ], function(Lvl) {
	Lvl.module('SavedItemsApp', function(SavedItemsApp, Lvl, Backbone, Marionette, $, _) {
		'use strict';

		/* Initialization & finalization */
		SavedItemsApp.startWithParent = false;

		SavedItemsApp.onStart = function() {
			console.log('starting SavedItemsApp');
		};

		SavedItemsApp.onStop = function() {
			console.log('stopping SavedItemsApp');
		};

		/* Commands and events */
		Lvl.commands.setHandler('saved-items:set:active', function(section) {
			require([ 'apps/saved-items/layout/saved-items_layout_ctrl' ], function(LayoutController) {
				SavedItemsApp.currentSection = LayoutController.showLayout(section);
			});
		});
	});
});