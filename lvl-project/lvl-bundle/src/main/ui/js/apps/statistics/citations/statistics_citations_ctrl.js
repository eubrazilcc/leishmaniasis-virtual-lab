/**
 * RequireJS module that defines the controller: statistics->citations.
 */

define([ 'app', 'apps/statistics/citations/statistics_citations_view' ], function(Lvl, View) {
	Lvl.module('StatisticsApp.Citations', function(Citations, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Citations.Controller = {
			showSection : function() {
				var view = new View.Content();
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.StatisticsApp.Citations.Controller;
});