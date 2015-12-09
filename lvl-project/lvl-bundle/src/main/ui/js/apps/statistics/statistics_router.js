/**
 * RequireJS module that defines the routes of the sub-application: statistics.
 */

define([ 'app', 'routefilter' ], function(Lvl) {
	Lvl.module('Routers.StatisticsApp', function(StatisticsAppRouter, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var Router = Backbone.Router.extend({
			routes : {
				'statistics' : 'showStatistics',
				'statistics/:section' : 'showStatistics'
			},
			before : function() {
				if (!Lvl.config.isAuthenticated()) {
					Lvl.navigate('login/' + encodeURIComponent(Backbone.history.fragment) + '/unauthenticated', {
						trigger : true,
						replace : true
					});
					return false;
				}
				require([ 'apps/statistics/statistics_app' ], function() {
					Lvl.execute('set:active:header', 'workspace', 'statistics');
					Lvl.execute('set:active:footer', 'workspace');
					Lvl.startSubApp('StatisticsApp');
				});
				return true;
			},
			showStatistics : function(section) {
				section = (section || 'sequences').toLowerCase();
				if (section === 'sequences') {
					Lvl.navigate('statistics/' + section, {
						trigger : false,
						replace : true
					});
					Lvl.execute('statistics:set:active', section);
				} else if (section === 'samples' || section === 'citations' || section === 'occurrences') {
					Lvl.execute('statistics:set:active', section);
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