/**
 * RequireJS module that defines the controller: open->layout.
 */

define([ 'app', 'apps/open/layout/open_layout_view', 'apps/open/layout/entities/layout', 'apps/open/layout/entities/event' ], function(Lvl, View, LayoutEntity,
		EventEntity) {
	Lvl.module('OpenContent.Layout', function(Show, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Show.Controller = {
			showLayout : function(options) {
				options = options || {};
				options.agenda = new EventEntity.EventCollection();
				var view = new View.Layout({
					model : new LayoutEntity.Layout(options)
				});
				Lvl.mainRegion.show(view);
			}
		}
	});
	return Lvl.OpenContent.Layout.Controller;
});