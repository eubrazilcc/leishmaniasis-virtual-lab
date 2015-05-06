/**
 * RequireJS module that defines the controller: admin->layout.
 */

define([ 'app', 'apps/admin/layout/admin_layout_view' ], function(Lvl, View) {
	Lvl.module('AdminApp.Layout', function(Layout, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Layout.Controller = {
			showLayout : function(section) {
				require([ 'apps/admin/' + section + '/admin_' + section + '_ctrl', 'apps/admin/layout/entities/tablinks' ],
						function(SectionController) {
							var tabLinks = Lvl.request('admin:navigation:entities');
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
						});
			}
		}
	});
	return Lvl.AdminApp.Layout.Controller;
});