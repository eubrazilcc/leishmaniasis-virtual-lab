/**
 * RequireJS module that defines the routes of the sub-application: collection.
 */

define([ 'app', 'routefilter' ], function(Lvl) {
	Lvl.module('Routers.CollectionApp', function(CollectionAppRouter, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var Router = Backbone.Router.extend({
			routes : {
				'collection' : 'showCollection',
				'collection/:section' : 'showCollection',
				'collection/:section/:subsection' : 'showCollection'
			},
			before : function() {
				if (!Lvl.config.isAuthenticated()) {
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
			showCollection : function(section, subsection) {
				section = (section || 'browse').toLowerCase();
				if (section === 'browse' || section === 'unseq' || section === 'pending') {
					subsection = (subsection || 'sandflies').toLowerCase();
					Lvl.navigate('collection/' + section + '/' + subsection, {
						trigger : false,
						replace : true
					});
					Lvl.execute('collection:set:active', section, subsection);
				} else if (section === 'map' || section === 'stats' || section === 'submit') {
					Lvl.execute('collection:set:active', section);
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