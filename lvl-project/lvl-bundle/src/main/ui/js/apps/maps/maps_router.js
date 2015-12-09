/**
 * RequireJS module that defines the routes of the sub-application: maps.
 */

define([ 'app', 'routefilter' ], function(Lvl) {
	Lvl.module('Routers.MapsApp', function(MapsAppRouter, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var Router = Backbone.Router.extend({
			routes : {
				'maps' : 'showMaps',
				'maps/:section' : 'showMaps'
			},
			before : function() {
				if (!Lvl.config.isAuthenticated()) {
					Lvl.navigate('login/' + encodeURIComponent(Backbone.history.fragment) + '/unauthenticated', {
						trigger : true,
						replace : true
					});
					return false;
				}
				require([ 'apps/maps/maps_app' ], function() {
					Lvl.execute('set:active:header', 'workspace', 'maps');
					Lvl.execute('set:active:footer', 'workspace');
					Lvl.startSubApp('MapsApp');
				});
				return true;
			},
			showMaps : function(section) {
				section = (section || 'datasets').toLowerCase();
				if (section === 'datasets') {
					Lvl.navigate('maps/' + section, {
						trigger : false,
						replace : true
					});
					Lvl.execute('maps:set:active', section);				
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