/**
 * RequireJS module that defines the controller: files->layout.
 */

define([ 'app', 'apps/files/layout/files_layout_view' ], function(Lvl, View) {
	Lvl.module('FilesApp.Layout', function(Layout, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Layout.Controller = {
			showLayout : function(section, id) {
				var subapp = section + (id ? '_item' : '');
				require([ 'apps/files/' + subapp + '/files_' + subapp + '_ctrl', 'apps/files/layout/entities/tablinks' ], function(SectionController) {
					var tabLinks = Lvl.request('files:navigation:entities');
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
	return Lvl.FilesApp.Layout.Controller;
});