/**
 * RequireJS module that defines the controller: software->show.
 */

define([ 'app', 'apps/software/show/software_show_view' ], function(Lvl, View) {
	Lvl.module('SoftwareApp.Show', function(Show, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Show.Controller = {
			showSoftware : function(section) {
				var section = section || 'software';
				var view = new View.Content({
					model : new Backbone.Model(),
					section : section
				});
				Lvl.mainRegion.show(view);
			}
		}
	});
	return Lvl.SoftwareApp.Show.Controller;
});