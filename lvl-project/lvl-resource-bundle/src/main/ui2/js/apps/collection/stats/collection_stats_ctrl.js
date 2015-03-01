/**
 * RequireJS module that defines the controller: collection->stats.
 */

define([ 'app', 'apps/config/marionette/configuration', 'entities/statistic', 'apps/collection/stats/collection_stats_view' ], function(Lvl, Configuration,
		StatsModel, View) {
	Lvl.module('CollectionApp.Stats', function(Stats, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Stats.Controller = {
			showSection : function() {
				var view = new View.Content({
					collection : new StatsModel.StatisticPageableCollection({
						oauth2_token : new Configuration().authorizationToken()
					})
				});
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.CollectionApp.Stats.Controller;
});