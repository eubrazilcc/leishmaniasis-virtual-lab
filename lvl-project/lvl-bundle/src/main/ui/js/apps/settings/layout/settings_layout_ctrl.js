/**
 * RequireJS module that defines the controller: settings->layout.
 */

define([ 'app', 'apps/settings/layout/settings_layout_view' ], function(Lvl, View) {
	Lvl.module('SettingsApp.Layout', function(Layout, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Layout.Controller = {
			showLayout : function(section) {
				var controller = function(SectionController) {
					var tabLinks = Lvl.request('settings:navigation:entities');
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
				case 'instances':
					require([ 'apps/settings/instances/settings_instances_ctrl', 'apps/settings/layout/entities/tablinks' ], controller);
					break;
				case 'account':
				default:
					require([ 'apps/settings/account/settings_account_ctrl', 'apps/settings/layout/entities/tablinks' ], controller);
					break;
				}
			}
		}
	});
	return Lvl.SettingsApp.Layout.Controller;
});