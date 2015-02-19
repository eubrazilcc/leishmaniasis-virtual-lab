/**
 * RequireJS module that defines the controller: not-found->show.
 */

define([ 'app', 'apps/not-found/show/not-found_show_view' ], function(Lvl, View) {
	Lvl.module('NotFoundApp.Show', function(Show, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Show.Controller = {
			showNotFound : function() {
				require([ 'entities/navigation' ], function() {
					var view = new View.Content({
						navigation : Lvl.request('navigation:links:entities'),
						settings : Lvl.request('navigation:settings:entities'),
						external : Lvl.request('navigation:external:entities')
					});
					Lvl.mainRegion.show(view);
				});
			}
		}
	});
	return Lvl.NotFoundApp.Show.Controller;
});