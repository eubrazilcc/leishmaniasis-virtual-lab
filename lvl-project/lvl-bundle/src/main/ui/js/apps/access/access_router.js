/**
 * RequireJS module that defines the routes of the sub-application: access.
 */

define([ 'app', 'routefilter' ], function(Lvl) {
	Lvl.module('Routers.AccessApp', function(LoginAppRouter, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var Router = Backbone.Router.extend({
			routes : {
				'login(/:target)(/:reason)' : 'showLogin',
				'auth/:provider/:section?email=:email&access_token=:access_token' : 'authorizationCallback',
				'register' : 'showRegistration',
				'account/validation(/:email)(/:code)' : 'validateAccount',
				'logout' : 'logout'
			},
			before : function(route) {
				require([ 'apps/access/access_app' ], function() {
					if (route !== 'logout') {
						Lvl.execute('set:active:header', 'no_header');
						Lvl.execute('set:active:footer', 'no_footer');
					}
					Lvl.startSubApp('AccessApp');
				});
			},
			showLogin : function(target, reason) {
				Lvl.execute('show:login', target, reason);
			},
			authorizationCallback : function(provider, section, email, access_token) {
				Lvl.execute('show:authz:callback', provider, section, email, access_token);
			},
			showRegistration : function() {
				Lvl.execute('show:registration');
			},
			validateAccount : function(email, code) {
				Lvl.execute('show:account:validation', email, code);
			},
			logout : function() {
				Lvl.config.deleteSession();
				Lvl.navigate('home', {
					trigger : true
				});
			}
		});
		Lvl.addInitializer(function() {
			var router = new Router();
		});
	});
});