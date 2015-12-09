/**
 * RequireJS module that defines the controller: e-compendium->pending.
 */

define([ 'app', 'apps/e-compendium/pending/e-compendium_pending_view' ], function(Lvl, View) {
	Lvl.module('CollectionApp.Pending', function(Pending, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Pending.Controller = {
			showSection : function() {
				var view = new View.Content();
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.CollectionApp.Pending.Controller;
});