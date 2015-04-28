/**
 * RequireJS module that defines the routes of the sub-application: e-compendium.
 */

define([ 'app', 'apps/config/marionette/configuration', 'routefilter' ], function(Lvl, Configuration) {
	Lvl.module('Routers.ECompendiumApp', function(ECompendiumAppRouter, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		var Router = Backbone.Router.extend({
			routes : {
				'e-compendium' : 'showECompendium',
				'e-compendium/:section' : 'showECompendium'
			},
			before : function() {
				if (!config.isAuthenticated()) {
					Lvl.navigate('login/' + encodeURIComponent(Backbone.history.fragment) + '/unauthenticated', {
						trigger : true,
						replace : true
					});
					return false;
				}
				require([ 'apps/e-compendium/e-compendium_app' ], function() {
					Lvl.execute('set:active:header', 'workspace', 'e-compendium');
					Lvl.execute('set:active:footer', 'workspace');
					Lvl.startSubApp('ECompendiumApp');
				});
				return true;
			},
			showECompendium : function(section) {
				section = (section || 'browse').toLowerCase();
				if (section === 'browse') {
					Lvl.navigate('e-compendium/' + section, {
						trigger : false,
						replace : true
					});
					Lvl.execute('e-compendium:set:active', section);
				} else if (section === 'map' || section === 'stats' || section === 'submit') {
					Lvl.execute('e-compendium:set:active', section);
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