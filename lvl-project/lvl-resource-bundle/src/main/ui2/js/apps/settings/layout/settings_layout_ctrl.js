/**
 * RequireJS module that defines the controller: settings->layout.
 */

define([ 'app', 'apps/settings/layout/settings_layout_view' ], function(Lvl, View) {
	Lvl.module('SettingsApp.Layout', function(Layout, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Layout.Controller = {
			showLayout : function(section) {
				require([ 'apps/settings/' + section + '/settings_' + section + '_ctrl', 'apps/settings/layout/entities/tablinks' ],
						function(SectionController) {
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
						});
			}
		}
	});
	return Lvl.SettingsApp.Layout.Controller;
});