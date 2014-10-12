/**
 * RequireJS module that defines the controller: analysis->layout.
 */

define([ 'app', 'apps/analysis/layout/analysis_layout_view' ], function(Lvl, View) {
	Lvl.module('AnalysisApp.Layout', function(Layout, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Layout.Controller = {
			showLayout : function(section, id) {
				var subapp = section + (id ? '_item' : '');
				require([ 'apps/analysis/' + subapp + '/analysis_' + subapp + '_ctrl', 'apps/analysis/layout/entities/tablinks' ], function(SectionController) {
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
				});
			}
		}
	});
	return Lvl.AnalysisApp.Layout.Controller;
});