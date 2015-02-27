/**
 * RequireJS module that defines the routes of the sub-application: settings.
 */

define([ 'app', 'apps/config/marionette/configuration', 'routefilter' ], function(Lvl, Configuration) {
	Lvl.module('Routers.SettingsApp', function(SettingsAppRouter, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		var Router = Backbone.Router.extend({
			routes : {
				'settings' : 'defaultSettings',
				'settings/account' : 'accountSettings',
				'settings/instances' : 'instancesSettings'
			},
			before : function() {
				if (!config.isAuthenticated()) {
					Lvl.navigate('login/' + encodeURIComponent(Backbone.history.fragment) + '/unauthenticated', {
						trigger : true,
						replace : true
					});
					return false;
				}
				require([ 'apps/settings/settings_app' ], function() {
					Lvl.execute('set:active:header', 'workspace', 'settings');
					Lvl.execute('set:active:footer', 'workspace');
					Lvl.startSubApp('SettingsApp');
				});
				return true;
			},
			defaultSettings : function() {
				var self = this;
				Lvl.navigate('settings/account', {
					trigger : true,
					replace : true
				});
			},
			accountSettings : function() {
				Lvl.execute('settings:set:active', 'account');
			},
			instancesSettings : function() {
				Lvl.execute('settings:set:active', 'instances');
			}
		});
		Lvl.addInitializer(function() {
			var router = new Router();
		});
	});
});