/**
 * RequireJS module that defines the sub-application: e-compendium.
 */

define([ 'app' ], function(Lvl) {
	Lvl.module('ECompendiumApp', function(ECompendiumApp, Lvl, Backbone, Marionette, $, _) {
		'use strict';

		/* Initialization & finalization */
		ECompendiumApp.startWithParent = false;

		ECompendiumApp.onStart = function() {
			console.log('starting ECompendiumApp');
		};

		ECompendiumApp.onStop = function() {
			console.log('stopping ECompendiumApp');
		};

		/* Commands and events */
		Lvl.commands.setHandler('e-compendium:set:active', function(section) {
			require([ 'apps/e-compendium/layout/e-compendium_layout_ctrl' ], function(LayoutController) {
				ECompendiumApp.currentSection = LayoutController.showLayout(section);
			});
		});
	});
});