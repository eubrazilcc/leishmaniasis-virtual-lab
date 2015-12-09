/**
 * RequireJS module that defines the controller: maps->layout.
 */

define([ 'app', 'apps/maps/layout/maps_layout_view' ], function(Lvl, View) {
	Lvl.module('MapsApp.Layout', function(Layout, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Layout.Controller = {
			showLayout : function(section, id) {
				var subapp = section + (id ? '_item' : '');
				var controller = function(SectionController) {
					var tabLinks = Lvl.request('maps:navigation:entities');
					var tabLinkToSelect = tabLinks.find(function(tabLink) {
						return tabLink.get('link') === section;
					});
					tabLinkToSelect.select();
					tabLinks.trigger('reset');
					var view = new View.Layout({
						navigation : tabLinks
					});
					Lvl.mainRegion.show(view);
					return SectionController.showSection(id);
				};
				switch (subapp) {				
				case 'datasets':
				default:
					require([ 'apps/maps/datasets/maps_datasets_ctrl', 'apps/maps/layout/entities/tablinks' ], controller);	
					break;
				}				
			}
		}
	});
	return Lvl.MapsApp.Layout.Controller;
});