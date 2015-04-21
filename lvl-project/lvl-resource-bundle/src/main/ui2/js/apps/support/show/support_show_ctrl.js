/**
 * RequireJS module that defines the controller: support->show.
 */

define([ 'app', 'apps/support/show/support_show_view' ], function(Lvl, View) {
	Lvl.module('SupportApp.Show', function(Show, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Show.Controller = {
			showSupport : function(section) {
				var section = section || 'support';
				var view = new View.Content({
					model : new Backbone.Model(),
					section : section
				});
				Lvl.mainRegion.show(view);
			}
		}
	});
	return Lvl.SupportApp.Show.Controller;
});