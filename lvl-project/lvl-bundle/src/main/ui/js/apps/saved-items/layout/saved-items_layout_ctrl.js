/**
 * RequireJS module that defines the controller: saved-items->layout.
 */

define([ 'app', 'apps/saved-items/layout/saved-items_layout_view' ], function(Lvl, View) {
	Lvl.module('SavedItemsApp.Layout', function(Layout, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Layout.Controller = {
			showLayout : function(section) {
				var controller = function(SectionController) {
					var tabLinks = Lvl.request('saved-items:navigation:entities');
					var tabLinkToSelect = tabLinks.find(function(tabLink) {
						return tabLink.get('link') === section;
					});
					tabLinkToSelect.select();
					tabLinks.trigger('reset');
					var view = new View.Layout({
						navigation : tabLinks
					});
					Lvl.mainRegion.show(view);
					return SectionController.showSection();
				};
				switch (section) {
				case 'searches':
				default:
					require([ 'apps/saved-items/searches/saved-items_searches_ctrl', 'apps/saved-items/layout/entities/tablinks' ], controller);
					break;
				}
			}
		}
	});
	return Lvl.SavedItemsApp.Layout.Controller;
});