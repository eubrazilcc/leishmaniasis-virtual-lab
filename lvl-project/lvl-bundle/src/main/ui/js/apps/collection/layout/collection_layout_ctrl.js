/**
 * RequireJS module that defines the controller: collection->layout.
 */

define([ 'app', 'apps/collection/layout/collection_layout_view' ], function(Lvl, View) {
	Lvl.module('CollectionApp.Layout', function(Layout, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Layout.Controller = {
			showLayout : function(section, id) {
				var controller = function(SectionController) {
					var tabLinks = Lvl.request('collection:navigation:entities');
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
				case 'samples':
					require([ 'apps/collection/samples/collection_samples_ctrl', 'apps/collection/layout/entities/tablinks' ], controller);
					break;
				case 'pending':
					require([ 'apps/collection/pending/collection_pending_ctrl', 'apps/collection/layout/entities/tablinks' ], controller);
					break;
				case 'submit':
					require([ 'apps/collection/submit/collection_submit_ctrl', 'apps/collection/layout/entities/tablinks' ], controller);
					break;
				case 'sequences':
				default:
					require([ 'apps/collection/sequences/collection_sequences_ctrl', 'apps/collection/layout/entities/tablinks' ], controller);
					break;
				}
			}
		}
	});
	return Lvl.CollectionApp.Layout.Controller;
});