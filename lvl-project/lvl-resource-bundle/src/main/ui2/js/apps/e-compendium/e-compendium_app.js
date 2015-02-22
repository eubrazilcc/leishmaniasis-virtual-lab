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

		ECompendiumApp.currentSection = null;

		/* Commands and events */
		Lvl.commands.setHandler('e-compendium:set:active', function(section) {
			section = section || 'default';
			if (ECompendiumApp.currentSection !== section) {
				if (section === 'browse' || section === 'map' || section === 'stats' || section === 'submit') {
					require([ 'apps/e-compendium/layout/e-compendium_layout_ctrl' ], function(LayoutController) {
						ECompendiumApp.currentSection = LayoutController.showLayout(section);
					});
				} else {
					Lvl.mainRegion.currentView.reset();
					ECompendiumApp.currentSection = null;
				}
			}
		});
	});
});