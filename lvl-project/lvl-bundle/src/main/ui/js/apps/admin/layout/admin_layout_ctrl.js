/**
 * RequireJS module that defines the controller: admin->layout.
 */

define([ 'app', 'apps/admin/layout/admin_layout_view' ], function(Lvl, View) {
	Lvl.module('AdminApp.Layout', function(Layout, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Layout.Controller = {
			showLayout : function(section) {
				var controller = function(SectionController) {
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
				};
				switch (section) {
				case 'subscription_requests':
					require([ 'apps/admin/subscription_requests/admin_subscription_requests_ctrl', 'apps/admin/layout/entities/tablinks' ], controller);
					break;
				case 'issues':
				default:
					require([ 'apps/admin/issues/admin_issues_ctrl', 'apps/admin/layout/entities/tablinks' ], controller);				
					break;
				}				
			}
		}
	});
	return Lvl.AdminApp.Layout.Controller;
});