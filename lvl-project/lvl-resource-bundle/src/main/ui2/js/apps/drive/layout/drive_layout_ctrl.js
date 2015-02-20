/**
 * RequireJS module that defines the controller: drive->layout.
 */

define([ 'app', 'apps/drive/layout/drive_layout_view' ], function(Lvl, View) {
	Lvl.module('DriveApp.Layout', function(Layout, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Layout.Controller = {
			showLayout : function(section, id) {
				var subapp = section + (id ? '_item' : '');
				require([ 'apps/drive/' + subapp + '/drive_' + subapp + '_ctrl', 'apps/drive/layout/entities/tablinks' ], function(SectionController) {
					var tabLinks = Lvl.request('drive:navigation:entities');
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
	return Lvl.DriveApp.Layout.Controller;
});