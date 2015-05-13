/**
 * RequireJS module that defines the controller: settings->account.
 */

define([ 'app', 'apps/settings/account/settings_account_view' ], function(Lvl, View) {
	Lvl.module('SettingsApp.Account', function(Account, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Account.Controller = {
			showSection : function() {
				require([ 'entities/user' ], function(UserModel) {
					var userModel = new UserModel.User({
						'email' : Lvl.config.session.get('user.session').email
					});
					userModel.oauth2_token = Lvl.config.authorizationToken();
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