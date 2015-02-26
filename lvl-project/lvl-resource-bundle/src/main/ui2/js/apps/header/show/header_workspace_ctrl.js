/**
 * RequireJS module that defines the controller: header->workspace.
 */

define([ 'app', 'apps/header/show/header_workspace_view' ], function(Lvl, View) {
	Lvl.module('HeaderApp.Workspace', function(Workspace, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Workspace.Controller = {
			showHeader : function(navLinks) {
				var view = new View.Header({
					navigation : navLinks
				});
				view.on('access:user:profile', function(accession) {
					require([ 'apps/access/profile/profile_viewer' ], function(UserProfileView) {						
						var dialogView = new UserProfileView.Content();
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