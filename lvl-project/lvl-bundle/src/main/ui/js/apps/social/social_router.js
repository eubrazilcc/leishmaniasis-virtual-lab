/**
 * RequireJS module that defines the routes of the sub-application: social.
 */

define([ 'app', 'routefilter' ], function(Lvl) {
	Lvl.module('Routers.SocialApp', function(SocialAppRouter, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var Router = Backbone.Router.extend({
			routes : {
				'social' : 'showSocial'
			},
			before : function() {
				if (!Lvl.config.isAuthenticated()) {
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