/**
 * RequireJS module that defines the sub-application: collection.
 */

define([ 'app' ], function(Lvl) {
	Lvl.module('CollectionApp', function(CollectionApp, Lvl, Backbone, Marionette, $, _) {
		'use strict';

		/* Initialization & finalization */
		CollectionApp.startWithParent = false;

		CollectionApp.onStart = function() {
			console.log('starting CollectionApp');
		};

		CollectionApp.onStop = function() {
			console.log('stopping CollectionApp');
		};

		CollectionApp.currentSection = null;

		/* Commands and events */
		Lvl.commands.setHandler('collection:set:active', function(section) {
			section = section || 'default';
			if (CollectionApp.currentSection !== section) {
				if (section === 'browse' || section === 'map' || section === 'stats' || section === 'submit') {
					require([ 'apps/collection/layout/collection_layout_ctrl' ], function(LayoutController) {
						CollectionApp.currentSection = LayoutController.showLayout(section);
					});
				} else {
					Lvl.mainRegion.currentView.reset();
					CollectionApp.currentSection = null;
				}
			}
		});
	});
});