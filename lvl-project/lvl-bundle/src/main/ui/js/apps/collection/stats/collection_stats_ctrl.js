/**
 * RequireJS module that defines the controller: collection->stats.
 */

define([ 'app', 'entities/statistic', 'apps/collection/stats/collection_stats_view' ], function(Lvl, StatsModel, View) {
	Lvl.module('CollectionApp.Stats', function(Stats, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Stats.Controller = {
			showSection : function() {
				var view = new View.Content({
					model : new StatsModel.Statistic({
						oauth2_token : Lvl.config.authorizationToken()
					})
				});
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.CollectionApp.Stats.Controller;
});