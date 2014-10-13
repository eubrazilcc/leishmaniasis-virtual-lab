/**
 * RequireJS module that defines the routes of the sub-application: files.
 */

define([ 'app', 'apps/config/marionette/configuration', 'routefilter' ], function(Lvl, Configuration) {
	Lvl.module('Routers.FilesApp', function(FilesAppRouter, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		var Router = Backbone.Router.extend({
			routes : {
				'files' : 'defaultFiles',
				'files/links' : 'showLinks'
			},
			before : function() {
				if (!config.isAuthenticated()) {
					Lvl.navigate('login/' + encodeURIComponent(Backbone.history.fragment) + '/unauthenticated', {
						trigger : true,
						replace : true
					});
					return false;
				}
				require([ 'apps/files/files_app' ], function() {
					Lvl.execute('set:active:header', 'workspace', 'files');
					Lvl.execute('set:active:footer', 'workspace');
					Lvl.startSubApp('FilesApp');
				});
				return true;
			},
			defaultFiles : function() {
				var self = this;
				Lvl.navigate('files/links', {
					trigger : true,
					replace : true
				});
			},
			showLinks : function() {
				Lvl.execute('files:set:active', 'links');
			}
		});
		Lvl.addInitializer(function() {
			var router = new Router();
		});
	});
});