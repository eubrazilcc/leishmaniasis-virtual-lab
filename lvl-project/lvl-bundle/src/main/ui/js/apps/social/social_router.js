/**
 * RequireJS module that defines the routes of the sub-application: social.
 */

define([ 'app', 'routefilter' ], function(Lvl) {
	Lvl.module('Routers.SocialApp', function(SocialAppRouter, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var Router = Backbone.Router.extend({
			routes : {
				'social' : 'showSocial',
				'social/:section' : 'showSocial'
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
			showSocial : function(section) {
				section = (section || 'posts').toLowerCase();
				if (section === 'posts') {
					Lvl.navigate('social/' + section, {
						trigger : false,
						replace : true
					});
					Lvl.execute('social:set:active', section);
				} else if (section === 'filters') {
					Lvl.execute('social:set:active', section);
				} else {
					Lvl.navigate('not-found', {
						trigger : true,
						replace : true
					});
				}
			}
		});
		Lvl.addInitializer(function() {
			var router = new Router();
		});
	});
});