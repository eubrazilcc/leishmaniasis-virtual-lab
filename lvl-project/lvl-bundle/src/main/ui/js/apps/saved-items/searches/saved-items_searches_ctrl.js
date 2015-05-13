/**
 * RequireJS module that defines the controller: saved-items->searches.
 */

define([ 'app', 'entities/saved_search', 'apps/saved-items/searches/saved-items_searches_view' ], function(Lvl, SearchEntity, View) {
	Lvl.module('SavedItemsApp.Searches', function(Searches, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Searches.Controller = {
			showSection : function() {
				var view = new View.Content({
					collection : new SearchEntity.SavedSearchPageableCollection({
						oauth2_token : Lvl.config.authorizationToken()
					})
				});
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.SavedItemsApp.Searches.Controller;
});