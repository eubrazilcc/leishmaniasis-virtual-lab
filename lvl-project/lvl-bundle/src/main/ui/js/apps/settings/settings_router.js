/**
 * RequireJS module that defines the routes of the sub-application: settings.
 */

define([ 'app', 'routefilter' ], function(Lvl) {
	Lvl.module('Routers.SettingsApp', function(SettingsAppRouter, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var Router = Backbone.Router.extend({
			routes : {
				'settings' : 'showSettings',
				'settings/:section' : 'showSettings'
			},
			before : function() {
				if (!Lvl.config.isAuthenticated()) {
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
			showSettings : function(section) {
				section = (section || 'account').toLowerCase();
				if (section === 'account') {
					Lvl.navigate('settings/' + section, {
						trigger : false,
						replace : true
					});
					Lvl.execute('settings:set:active', section);
				} else if (section === 'instances') {
					Lvl.execute('settings:set:active', section);
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