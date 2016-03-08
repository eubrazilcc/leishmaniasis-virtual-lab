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
				var fragment = Backbone.history.fragment, slashCount = (fragment.match(/\//g) || []).length, section, collectionName;
				switch (slashCount) {
				case 0:
					fragment = fragment + '/sequences/sandflies';
					break;
				case 1:
					fragment = fragment + '/sandflies';
					break;
				default:
				}
				var args = this._extractParameters(this._routeToRegExp('collection/:section/:subsection'), fragment);
				if (Array.isArray(args) && args.length >= 2) {
					section = args[0];
					collectionName = args[1];
				}
				require([ 'apps/collection/collection_app' ], function() {
					Lvl.execute('set:active:header', 'workspace', 'collection', section, collectionName);
					Lvl.execute('set:active:footer', 'workspace');
					Lvl.startSubApp('CollectionApp');
				});
				return true;
			},
			showCollection : function(section, subsection) {
				section = (section || 'sequences').toLowerCase();
				if (section === 'sequences' || section === 'samples' || section === 'pending') {
					subsection = (subsection || 'sandflies').toLowerCase();
					Lvl.navigate('collection/' + section + '/' + subsection, {
						trigger : false,
						replace : true
					});
					Lvl.execute('collection:set:active', section, subsection);					
				} else if (section === 'submit') {
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