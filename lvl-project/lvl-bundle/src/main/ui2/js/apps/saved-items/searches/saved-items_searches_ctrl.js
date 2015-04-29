/**
 * RequireJS module that defines the controller: saved-items->searches.
 */

define([ 'app', 'apps/config/marionette/configuration', 'entities/saved_search', 'apps/saved-items/searches/saved-items_searches_view' ], function(Lvl,
		Configuration, SearchEntity, View) {
	Lvl.module('SavedItemsApp.Searches', function(Searches, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		Searches.Controller = {
			showSection : function() {
				var view = new View.Content({
					collection : new SearchEntity.SavedSearchPageableCollection({
						oauth2_token : config.authorizationToken()
					})
				});
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.SavedItemsApp.Searches.Controller;
});