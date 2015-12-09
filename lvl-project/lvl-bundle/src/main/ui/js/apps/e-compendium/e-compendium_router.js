/**
 * RequireJS module that defines the routes of the sub-application: e-compendium.
 */

define([ 'app', 'routefilter' ], function(Lvl) {
	Lvl.module('Routers.ECompendiumApp', function(ECompendiumAppRouter, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var Router = Backbone.Router.extend({
			routes : {
				'e-compendium' : 'showECompendium',
				'e-compendium/:section' : 'showECompendium'
			},
			before : function() {
				if (!Lvl.config.isAuthenticated()) {
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
				section = (section || 'citations').toLowerCase();				
				if (section === 'citations') {
					Lvl.navigate('e-compendium/' + section, {
						trigger : false,
						replace : true
					});
					Lvl.execute('e-compendium:set:active', section);
				} else if (section === 'pending' || section === 'submit') {
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