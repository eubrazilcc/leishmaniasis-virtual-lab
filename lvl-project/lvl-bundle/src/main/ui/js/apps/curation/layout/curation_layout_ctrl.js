/**
 * RequireJS module that defines the controller: curation->layout.
 */

define([ 'app', 'apps/curation/layout/curation_layout_view' ], function(Lvl, View) {
	Lvl.module('CurationApp.Layout', function(Layout, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Layout.Controller = {
			showLayout : function(section, id) {
				var controller = function(SectionController) {
					var tabLinks = Lvl.request('curation:navigation:entities');
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
				switch (section) {
				case 'submitted_citations':				
					require([ 'apps/curation/submitted_citations/curation_submitted_citations_ctrl', 'apps/curation/layout/entities/tablinks' ], controller);				
					break;
				case 'submitted_sequences':
				default:
					require([ 'apps/curation/submitted_sequences/curation_submitted_sequences_ctrl', 'apps/curation/layout/entities/tablinks' ], controller);
					break;				
				}				
			}
		}
	});
	return Lvl.CurationApp.Layout.Controller;
});