/**
 * RequireJS module that defines the routes of the sub-application: saved-items.
 */

define([ 'app', 'routefilter' ], function(Lvl) {
	Lvl.module('Routers.SavedItemsApp', function(SavedItemsAppRouter, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var Router = Backbone.Router.extend({
			routes : {
				'saved-items' : 'showSavedItems',
				'saved-items/:section' : 'showSavedItems'
			},
			before : function() {
				if (!Lvl.config.isAuthenticated()) {
					Lvl.navigate('login/' + encodeURIComponent(Backbone.history.fragment) + '/unauthenticated', {
						trigger : true,
						replace : true
					});
					return false;
				}
				require([ 'apps/saved-items/saved-items_app' ], function() {
					Lvl.execute('set:active:header', 'workspace', 'saved-items');
					Lvl.execute('set:active:footer', 'workspace');
					Lvl.startSubApp('SavedItemsApp');
				});
				return true;
			},
			showSavedItems : function(section) {
				section = (section || 'searches').toLowerCase();
				if (section === 'searches') {
					Lvl.navigate('saved-items/' + section, {
						trigger : false,
						replace : true
					});
					Lvl.execute('saved-items:set:active', section);
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