/**
 * RequireJS module that defines the controller: statistics->occurrences.
 */

define([ 'app', 'apps/statistics/occurrences/statistics_occurrences_view' ], function(Lvl, View) {
	Lvl.module('StatisticsApp.Occurrences', function(Occurrences, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Occurrences.Controller = {
			showSection : function() {
				var view = new View.Content();
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.StatisticsApp.Occurrences.Controller;
});