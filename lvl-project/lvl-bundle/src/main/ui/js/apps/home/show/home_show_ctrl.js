/**
 * RequireJS module that defines the controller: home->show.
 */

define([ 'app', 'apps/home/show/home_show_view' ], function(Lvl, View) {
	Lvl.module('HomeApp.Show', function(Show, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var lvlEndpoint = Lvl.config.get('endpoint', '');
		Show.Controller = {
			showHome : function() {
				var view = new View.Content({
					model : new Backbone.Model({
						endpoint : lvlEndpoint
					})
				});
				Lvl.mainRegion.show(view);
			}
		}
	});
	return Lvl.HomeApp.Show.Controller;
});