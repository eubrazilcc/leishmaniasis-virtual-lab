/**
 * RequireJS module that defines the controller: statistics->samples.
 */

define([ 'app', 'apps/statistics/samples/statistics_samples_view' ], function(Lvl, View) {
	Lvl.module('StatisticsApp.Samples', function(Samples, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Samples.Controller = {
			showSection : function() {
				var view = new View.Content();
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.StatisticsApp.Samples.Controller;
});