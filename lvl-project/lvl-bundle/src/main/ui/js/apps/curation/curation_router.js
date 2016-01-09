/**
 * RequireJS module that defines the routes of the sub-application: curation.
 */

define([ 'app', 'routefilter' ], function(Lvl) {
	Lvl.module('Routers.CurationApp', function(CurationAppRouter, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var Router = Backbone.Router.extend({
			routes : {
				'curation' : 'showCuration',
				'curation/:section' : 'showCuration'
			},
			before : function() {
				if (!Lvl.config.isAuthenticated()) {
					Lvl.navigate('login/' + encodeURIComponent(Backbone.history.fragment) + '/unauthenticated', {
						trigger : true,
						replace : true
					});
					return false;
				}
				require([ 'apps/curation/curation_app' ], function() {
					Lvl.execute('set:active:header', 'curation');
					Lvl.execute('set:active:footer', 'home');
					Lvl.startSubApp('CurationApp');
				});
				return true;
			},
			showCuration : function(section) {
				section = (section || 'submitted_sequences').toLowerCase();
				if (section === 'submitted_sequences') {
					Lvl.navigate('curation/' + section, {
						trigger : false,
						replace : true
					});
					Lvl.execute('curation:set:active', section);
				} else if (section === 'submitted_citations') {
					Lvl.execute('curation:set:active', 'submitted_citations');
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