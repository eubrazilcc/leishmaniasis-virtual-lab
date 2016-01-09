/**
 * RequireJS module that defines the controller: social->layout.
 */

define([ 'app', 'apps/social/layout/social_layout_view' ], function(Lvl, View) {
	Lvl.module('SocialApp.Layout', function(Layout, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Layout.Controller = {
			showLayout : function(section, id) {
				var subapp = section + (id ? '_item' : '');
				var controller = function(SectionController) {
					var tabLinks = Lvl.request('social:navigation:entities');
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
				case 'posts':
				default:
					require([ 'apps/social/posts/social_posts_ctrl', 'apps/social/layout/entities/tablinks' ], controller);	
					break;
				}				
			}
		}
	});
	return Lvl.SocialApp.Layout.Controller;
});