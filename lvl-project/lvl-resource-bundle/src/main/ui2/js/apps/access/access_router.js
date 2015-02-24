/**
 * RequireJS module that defines the routes of the sub-application: access.
 */

define([ 'app', 'routefilter' ], function(Lvl) {
	Lvl.module('Routers.AccessApp', function(LoginAppRouter, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var Router = Backbone.Router.extend({
			routes : {
				'login(/:target)(/:reason)' : 'showLogin',
				'auth/:service' : 'showAuthorization',
				'register' : 'showRegistration',
				'account/validation(/:email)(/:code)' : 'validateAccount',
				'logout' : 'logout'
			},
			before : function() {
				require([ 'apps/access/access_app' ], function() {
					Lvl.execute('set:active:header', 'no_header');
					Lvl.execute('set:active:footer', 'no_footer');
					Lvl.startSubApp('AccessApp');
				});
			},
			showLogin : function(target, reason) {
				Lvl.execute('show:login', target, reason);
			},
			showAuthorization : function(service) {
				// TODO
				console.log('Unsupported feature requested -- external authorization with: ' + service);
				// TODO
			},
			showRegistration : function() {
				Lvl.execute('show:registration');
			},
			validateAccount : function(email, code) {
				Lvl.execute('show:account:validation', email, code);
			},
			logout : function() {
				require([ 'apps/config/marionette/configuration' ], function(Configuration) {
					new Configuration().deleteSession();
				});
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