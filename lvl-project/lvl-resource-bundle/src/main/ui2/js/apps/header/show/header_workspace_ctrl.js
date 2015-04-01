/**
 * RequireJS module that defines the controller: header->workspace.
 */

define([ 'app', 'apps/config/marionette/configuration', 'apps/header/show/header_workspace_view' ], function(Lvl, Configuration, View) {
	Lvl.module('HeaderApp.Workspace', function(Workspace, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		Workspace.Controller = {
			showHeader : function(navLinks) {
				var docLinks = Lvl.request('navigation:documentation:entities');
				var view = new View.Header({
					navigation : navLinks,
					collection : docLinks
				});
				view.on('access:user:profile', function(accession) {
					require([ 'apps/access/profile/profile_viewer', 'entities/user' ], function(UserProfileView, UserModel) {
						var userModel = new UserModel.User({
							'email' : config.session.get('user.session').email
						});
						userModel.oauth2_token = new Configuration().authorizationToken();										
						var dialogView = new UserProfileView.Content({
							model : userModel
						});
						Lvl.dialogRegion.show(dialogView);
					});
				});
				Lvl.headerRegion.show(view);
				return View.id;
			}
		}
	});
	return Lvl.HeaderApp.Workspace.Controller;
});