/**
 * RequireJS module that defines the routes of the sub-application: admin.
 */

define([ 'app', 'apps/config/marionette/configuration', 'routefilter' ], function(Lvl, Configuration) {
	Lvl.module('Routers.AdminApp', function(AdminAppRouter, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		var Router = Backbone.Router.extend({
			routes : {
				'admin' : 'showAdmin',
				'admin/:section' : 'showAdmin'
			},
			before : function() {
				if (!config.isAuthenticated()) {
					Lvl.navigate('login/' + encodeURIComponent(Backbone.history.fragment) + '/unauthenticated', {
						trigger : true,
						replace : true
					});
					return false;
				}
				require([ 'apps/admin/admin_app' ], function() {
					Lvl.execute('set:active:header', 'admin');
					Lvl.execute('set:active:footer', 'home');
					Lvl.startSubApp('AdminApp');
				});
				return true;
			},
			showAdmin : function(section) {
				section = (section || 'issues').toLowerCase();
				if (section === 'issues') {
					Lvl.navigate('admin/' + section, {
						trigger : false,
						replace : true
					});
					Lvl.execute('admin:set:active', section);				
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