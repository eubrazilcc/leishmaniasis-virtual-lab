/**
 * RequireJS module that defines the controller: e-compendium->pending.
 */

define([ 'app', 'entities/pending_citation', 'apps/e-compendium/pending/e-compendium_pending_view' ], function(Lvl, PendingCitationModel, View) {
	Lvl.module('ECompendiumApp.Pending', function(Pending, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Pending.Controller = {
			showSection : function() {
				var view = new View.Content({
					collection : new PendingCitationModel.PendingCitationPageableCollection({
						oauth2_token : Lvl.config.authorizationToken()
					})
				});
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.ECompendiumApp.Pending.Controller;
});