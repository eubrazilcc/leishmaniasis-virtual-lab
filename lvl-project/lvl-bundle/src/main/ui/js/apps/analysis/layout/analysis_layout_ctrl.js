/**
 * RequireJS module that defines the controller: analysis->layout.
 */

define([ 'app', 'apps/analysis/layout/analysis_layout_view' ], function(Lvl, View) {
	Lvl.module('AnalysisApp.Layout', function(Layout, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Layout.Controller = {
			showLayout : function(section, id) {
				var subapp = section + (id ? '_item' : '');
				var controller = function(SectionController) {
					var tabLinks = Lvl.request('analysis:navigation:entities');
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
				case 'runs':
					require([ 'apps/analysis/runs/analysis_runs_ctrl', 'apps/analysis/layout/entities/tablinks' ], controller);
					break;
				case 'runs_item':
					require([ 'apps/analysis/runs_item/analysis_runs_item_ctrl', 'apps/analysis/layout/entities/tablinks' ], controller);
					break;				
				case 'pipelines':
				default:
					require([ 'apps/analysis/pipelines/analysis_pipelines_ctrl', 'apps/analysis/layout/entities/tablinks' ], controller);
					break;
				}
			}
		}
	});
	return Lvl.AnalysisApp.Layout.Controller;
});