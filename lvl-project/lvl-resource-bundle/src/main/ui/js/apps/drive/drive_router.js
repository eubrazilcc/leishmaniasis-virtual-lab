/**
 * RequireJS module that defines the routes of the sub-application: drive.
 */

define([ 'app', 'apps/config/marionette/configuration', 'routefilter' ], function(Lvl, Configuration) {
	Lvl.module('Routers.DriveApp', function(DriveAppRouter, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		var Router = Backbone.Router.extend({
			routes : {
				'drive' : 'defaultDrive',
				'drive/files' : 'showFiles',
				'drive/links' : 'showLinks'
			},
			before : function() {
				if (!config.isAuthenticated()) {
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
			defaultDrive : function() {
				var self = this;
				Lvl.navigate('drive/files', {
					trigger : true,
					replace : true
				});
			},
			showFiles : function() {
				Lvl.execute('drive:set:active', 'files');
			},
			showLinks : function() {
				Lvl.execute('drive:set:active', 'links');
			}
		});
		Lvl.addInitializer(function() {
			var router = new Router();
		});
	});
});