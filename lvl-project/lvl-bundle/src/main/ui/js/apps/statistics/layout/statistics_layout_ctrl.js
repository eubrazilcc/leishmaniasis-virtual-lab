/**
 * RequireJS module that defines the controller: statistics->layout.
 */

define([ 'app', 'apps/statistics/layout/statistics_layout_view' ], function(Lvl, View) {
	Lvl.module('StatisticsApp.Layout', function(Layout, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Layout.Controller = {
			showLayout : function(section, id) {
				var subapp = section + (id ? '_item' : '');
				var controller = function(SectionController) {
					var tabLinks = Lvl.request('statistics:navigation:entities');
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
				case 'samples':
					require([ 'apps/statistics/samples/statistics_samples_ctrl', 'apps/statistics/layout/entities/tablinks' ], controller);
					break;
				case 'citations':
					require([ 'apps/statistics/citations/statistics_citations_ctrl', 'apps/statistics/layout/entities/tablinks' ], controller);
					break;
				case 'occurrences':
					require([ 'apps/statistics/occurrences/statistics_occurrences_ctrl', 'apps/statistics/layout/entities/tablinks' ], controller);
					break;
				case 'sequences':
				default:
					require([ 'apps/statistics/sequences/statistics_sequences_ctrl', 'apps/statistics/layout/entities/tablinks' ], controller);	
					break;
				}				
			}
		}
	});
	return Lvl.StatisticsApp.Layout.Controller;
});