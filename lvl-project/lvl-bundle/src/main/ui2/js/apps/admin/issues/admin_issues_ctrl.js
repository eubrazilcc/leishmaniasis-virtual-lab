/**
 * RequireJS module that defines the controller: admin->issues.
 */

define([ 'app', 'apps/config/marionette/configuration', 'entities/saved_search', 'apps/admin/issues/admin_issues_view' ], function(Lvl,
		Configuration, SearchEntity, View) {
	Lvl.module('AdminApp.Searches', function(Searches, Lvl, Backbone, Marionette, $, _) {
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
	return Lvl.AdminApp.Searches.Controller;
});