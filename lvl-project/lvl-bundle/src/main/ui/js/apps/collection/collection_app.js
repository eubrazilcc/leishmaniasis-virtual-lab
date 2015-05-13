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

		/* Commands and events */
		Lvl.commands.setHandler('collection:set:active', function(section, subsection) {
			require([ 'apps/collection/layout/collection_layout_ctrl' ], function(LayoutController) {
				CollectionApp.currentSection = LayoutController.showLayout(section, subsection);
			});			
		});
	});
});