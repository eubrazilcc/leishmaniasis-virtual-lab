/**
 * RequireJS module that defines the controller: saved-items->searches.
 */

define([ 'app', 'apps/config/marionette/configuration', 'apps/saved-items/searches/saved-items_searches_view' ], function(Lvl, Configuration, View) {
	Lvl.module('SavedItemsApp.Searches', function(Searches, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		Searches.Controller = {
			showSection : function() {
				require([ 'entities/user' ], function(UserModel) {
					var userModel = new UserModel.User({
						'email' : config.session.get('user.session').email
					});
					userModel.oauth2_token = new Configuration().authorizationToken();
					var view = new View.Content({
						model : userModel
					});
					Lvl.mainRegion.currentView.tabContent.show(view);
				});
				return View.Content.id;
			}
		}
	});
	return Lvl.SavedItemsApp.Searches.Controller;
});