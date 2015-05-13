/**
 * RequireJS module that defines the routes of the sub-application: drive.
 */

define([ 'app', 'routefilter' ], function(Lvl) {
	Lvl.module('Routers.DriveApp', function(DriveAppRouter, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var Router = Backbone.Router.extend({
			routes : {
				'drive' : 'showDrive',
				'drive/:section' : 'showDrive'
			},
			before : function() {
				if (!Lvl.config.isAuthenticated()) {
					Lvl.navigate('login/' + encodeURIComponent(Backbone.history.fragment) + '/unauthenticated', {
						trigger : true,
						replace : true
					});
					return false;
				}
				require([ 'apps/drive/drive_app' ], function() {
					Lvl.execute('set:active:header', 'workspace', 'drive');
					Lvl.execute('set:active:footer', 'workspace');
					Lvl.startSubApp('DriveApp');
				});
				return true;
			},
			showDrive : function(section) {
				section = (section || 'datasets').toLowerCase();
				if (section === 'datasets') {
					Lvl.navigate('drive/' + section, {
						trigger : false,
						replace : true
					});
					Lvl.execute('drive:set:active', section);
				} else if (section === 'links') {
					Lvl.execute('drive:set:active', section);
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