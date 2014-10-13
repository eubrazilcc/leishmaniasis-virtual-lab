/**
 * RequireJS module that defines the controller: files->links.
 */

define([ 'app', 'apps/config/marionette/configuration', 'entities/link', 'apps/files/links/files_links_view' ], function(Lvl, Configuration, LinkModel, View) {
	Lvl.module('FilesApp.Links', function(Links, Lvl, Backbone, Marionette, $, _) {
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
	return Lvl.FilesApp.Links.Controller;
});