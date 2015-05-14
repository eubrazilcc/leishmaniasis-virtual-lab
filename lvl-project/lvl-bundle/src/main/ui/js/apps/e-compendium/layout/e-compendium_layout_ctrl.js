/**
 * RequireJS module that defines the controller: e-compendium->layout.
 */

define([ 'app', 'apps/e-compendium/layout/e-compendium_layout_view' ], function(Lvl, View) {
	Lvl.module('ECompendiumApp.Layout', function(Layout, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Layout.Controller = {
			showLayout : function(section) {
				var controller = function(SectionController) {
					var tabLinks = Lvl.request('e-compendium:navigation:entities');
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
				case 'map':
					require([ 'apps/e-compendium/map/e-compendium_map_ctrl', 'apps/e-compendium/layout/entities/tablinks' ], controller);
					break;
				case 'stats':
					require([ 'apps/e-compendium/stats/e-compendium_stats_ctrl', 'apps/e-compendium/layout/entities/tablinks' ], controller);
					break;
				case 'submit':
					require([ 'apps/e-compendium/submit/e-compendium_submit_ctrl', 'apps/e-compendium/layout/entities/tablinks' ], controller);
					break;
				case 'browse':
				default:
					require([ 'apps/e-compendium/browse/e-compendium_browse_ctrl', 'apps/e-compendium/layout/entities/tablinks' ], controller);
					break;
				}
			}
		}
	});
	return Lvl.ECompendiumApp.Layout.Controller;
});