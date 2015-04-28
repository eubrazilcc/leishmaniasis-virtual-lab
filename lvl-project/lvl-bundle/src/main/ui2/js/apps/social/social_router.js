/**
 * RequireJS module that defines the routes of the sub-application: social.
 */

define([ 'app', 'apps/config/marionette/configuration', 'routefilter' ], function(Lvl, Configuration) {
	Lvl.module('Routers.SocialApp', function(SocialAppRouter, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		var Router = Backbone.Router.extend({
			routes : {
				'social' : 'showSocial'
			},
			before : function() {
				if (!config.isAuthenticated()) {
					Lvl.navigate('login/' + encodeURIComponent(Backbone.history.fragment) + '/unauthenticated', {
						trigger : true,
						replace : true
					});
					return false;
				}
				require([ 'apps/social/social_app' ], function() {
					Lvl.execute('set:active:header', 'workspace', 'social');
					Lvl.execute('set:active:footer', 'workspace');
					Lvl.startSubApp('SocialApp');
				});
				return true;
			},
			showSocial : function() {
				Lvl.execute('show:social');
			}
		});

		Lvl.addInitializer(function() {
			var router = new Router();
		});
	});
});