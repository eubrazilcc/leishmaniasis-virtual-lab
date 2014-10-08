/**
 * RequireJS module that defines the routes of the sub-application: links.
 */

define([ 'app', 'apps/config/marionette/configuration', 'routefilter' ], function(Lvl, Configuration) {
	Lvl.module('Routers.LinksApp', function(LinksAppRouter, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var Router = Backbone.Router.extend({
			routes : {
				'links' : 'showLinks'
			},
			before : function() {
				require([ 'apps/links/links_app' ], function() {
					Lvl.execute('set:active:header', 'workspace', 'links');
					Lvl.execute('set:active:footer', 'workspace');
					Lvl.startSubApp('LinksApp');
				});
			},
			showLinks : function() {
				Lvl.execute('show:links');
			}
		});
		Lvl.addInitializer(function() {
			var router = new Router();
		});
	});
});