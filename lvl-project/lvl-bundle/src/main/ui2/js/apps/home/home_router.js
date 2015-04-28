/**
 * RequireJS module that defines the routes of the sub-application: home.
 */

define([ 'app', 'routefilter' ], function(Lvl) {
	Lvl.module('Routers.HomeApp', function(HomeAppRouter, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var Router = Backbone.Router.extend({
			routes : {
				'home' : 'showHome'
			},
			before : function() {
				require([ 'apps/home/home_app' ], function() {
					Lvl.execute('set:active:header', 'home');
					Lvl.execute('set:active:footer', 'home');
					Lvl.startSubApp('HomeApp');
				});
			},
			showHome : function() {
				Lvl.execute('show:home');
			}
		});

		Lvl.addInitializer(function() {
			var router = new Router();
		});
	});
});