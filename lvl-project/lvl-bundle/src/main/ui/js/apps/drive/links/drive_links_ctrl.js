/**
 * RequireJS module that defines the controller: drive->links.
 */

define([ 'app', 'entities/link', 'apps/drive/links/drive_links_view' ], function(Lvl, LinkModel, View) {
	Lvl.module('DriveApp.Links', function(Links, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Links.Controller = {
			showSection : function() {
				var view = new View.Content({
					collection : new LinkModel.LinkPageableCollection({
						oauth2_token : Lvl.config.authorizationToken()
					})
				});
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.DriveApp.Links.Controller;
});