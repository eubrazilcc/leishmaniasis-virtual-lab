/**
 * RequireJS module that defines the controller: statistics->sequences.
 */

define([ 'app', 'entities/statistic', 'apps/statistics/sequences/statistics_sequences_view' ], function(Lvl, StatsModel, View) {
	Lvl.module('StatisticsApp.Sequences', function(Stats, Lvl, Backbone, Marionette, $, _) {
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
	return Lvl.StatisticsApp.Sequences.Controller;
});