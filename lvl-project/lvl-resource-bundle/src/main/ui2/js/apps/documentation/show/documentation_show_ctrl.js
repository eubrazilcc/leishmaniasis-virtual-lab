/**
 * RequireJS module that defines the controller: documentation->show.
 */

define([ 'app', 'apps/documentation/show/documentation_show_view' ], function(Lvl, View) {
	Lvl.module('DocumentationApp.Show', function(Show, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Show.Controller = {
			showDocumentation : function(section) {
				var section = section || 'documentation';
				var view = new View.Content({
					model : new Backbone.Model(),
					section : section
				});
				Lvl.mainRegion.show(view);
			}
		}
	});
	return Lvl.DocumentationApp.Show.Controller;
});