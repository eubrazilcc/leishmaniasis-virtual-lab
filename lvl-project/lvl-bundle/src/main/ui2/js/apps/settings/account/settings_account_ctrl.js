/**
 * RequireJS module that defines the controller: settings->account.
 */

define([ 'app', 'apps/config/marionette/configuration', 'apps/settings/account/settings_account_view' ], function(Lvl, Configuration, View) {
	Lvl.module('SettingsApp.Account', function(Account, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		Account.Controller = {
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
	return Lvl.SettingsApp.Account.Controller;
});