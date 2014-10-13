/**
 * RequireJS module that defines the controller: e-compendium->browse.
 */

define([ 'app', 'apps/config/marionette/configuration', 'entities/reference', 'apps/e-compendium/browse/e-compendium_browse_view' ], function(Lvl,
		Configuration, ReferenceModel, View) {
	Lvl.module('ECompendiumApp.Browse', function(Browse, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		Browse.Controller = {
			showSection : function() {
				var view = new View.Content({
					collection : new ReferenceModel.ReferencePageableCollection({
						oauth2_token : config.authorizationToken()
					})
				});
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.ECompendiumApp.Browse.Controller;
});