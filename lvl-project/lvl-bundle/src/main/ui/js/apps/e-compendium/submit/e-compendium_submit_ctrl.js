/**
 * RequireJS module that defines the controller: e-compendium->submit.
 */

define([ 'app', 'apps/e-compendium/submit/e-compendium_submit_view' ], function(Lvl, View) {
	Lvl.module('ECompendiumApp.Submit', function(Submit, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Submit.Controller = {
			showSection : function() {
				var view = new View.Content();
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.ECompendiumApp.Submit.Controller;
});