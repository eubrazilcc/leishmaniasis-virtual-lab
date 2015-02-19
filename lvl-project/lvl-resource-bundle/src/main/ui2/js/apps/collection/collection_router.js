/**
 * RequireJS module that defines the routes of the sub-application: collection.
 */

define([ 'app', 'apps/config/marionette/configuration', 'routefilter' ], function(Lvl, Configuration) {
	Lvl.module('Routers.CollectionApp', function(CollectionAppRouter, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		var Router = Backbone.Router.extend({
			routes : {
				'collection' : 'defaultCollection',
				'collection/browse' : 'browseCollection',
				'collection/map' : 'mapCollection',
				'collection/stats' : 'statsCollection',
				'collection/submit' : 'submitCollection'
			},
			before : function() {
				if (!config.isAuthenticated()) {
					Lvl.navigate('login/' + encodeURIComponent(Backbone.history.fragment) + '/unauthenticated', {
						trigger : true,
						replace : true
					});
					return false;
				}
				require([ 'apps/collection/collection_app' ], function() {
					Lvl.execute('set:active:header', 'workspace', 'collection');
					Lvl.execute('set:active:footer', 'workspace');
					Lvl.startSubApp('CollectionApp');
				});
				return true;
			},
			defaultCollection : function() {
				var self = this;
				Lvl.navigate('collection/browse', {
					trigger : true,
					replace : true
				});
			},
			browseCollection : function() {
				Lvl.execute('collection:set:active', 'browse');
			},
			mapCollection : function() {
				Lvl.execute('collection:set:active', 'map');
			},
			statsCollection : function() {
				Lvl.execute('collection:set:active', 'stats');
			},
			submitCollection : function() {
				Lvl.execute('collection:set:active', 'submit');
			}
		});
		Lvl.addInitializer(function() {
			var router = new Router();
		});
	});
});