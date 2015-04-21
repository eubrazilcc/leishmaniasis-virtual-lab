/**
 * RequireJS module that defines the controller: open->layout.
 */

define([ 'app', 'apps/open/layout/open_layout_view', 'apps/open/layout/entities/layout', 'apps/open/layout/entities/event' ], function(Lvl, View, LayoutEntity,
		EventEntity) {
	Lvl.module('OpenContent.Layout', function(Show, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Show.Controller = {
			showLayout : function(name, application, section, main_section_view) {
				var view = new View.Layout({
					model : new LayoutEntity.Layout({
						name : name || 'Unknown',
						application : (application || 'unknown').toLowerCase(),
						section : (section || 'unknown').toLowerCase(),
						agenda : new EventEntity.EventCollection(),
					}),
					main_section_view : main_section_view
				});
				Lvl.mainRegion.show(view);
			}
		}
	});
	return Lvl.OpenContent.Layout.Controller;
});