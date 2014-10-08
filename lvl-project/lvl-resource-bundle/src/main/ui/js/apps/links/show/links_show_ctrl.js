/**
 * RequireJS module that defines the controller: links->show.
 */

define([ 'app', 'apps/config/marionette/configuration', 'entities/link', 'apps/links/show/links_show_view' ], function(Lvl, Configuration, LinkModel, View) {
	Lvl.module('LinksApp.Show', function(Show, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		Show.Controller = {
			showLinks : function() {
				var view = new View.Content({
					collection : new LinkModel.LinkPageableCollection({
						oauth2_token : config.authorizationToken()
					})
				});
				Lvl.mainRegion.show(view);
			}
		}
	});
	return Lvl.LinksApp.Show.Controller;
});