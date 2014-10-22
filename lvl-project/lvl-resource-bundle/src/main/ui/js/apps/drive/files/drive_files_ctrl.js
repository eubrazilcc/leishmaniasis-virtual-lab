/**
 * RequireJS module that defines the controller: drive->files.
 */

define([ 'app', 'apps/config/marionette/configuration', 'entities/link', 'apps/drive/links/drive_links_view' ], function(Lvl, Configuration, LinkModel, View) {
	Lvl.module('DriveApp.Links', function(Links, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		Links.Controller = {
			showSection : function() {
				var view = new View.Content({
					collection : new LinkModel.LinkPageableCollection({
						oauth2_token : config.authorizationToken()
					})
				});
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.DriveApp.Links.Controller;
});