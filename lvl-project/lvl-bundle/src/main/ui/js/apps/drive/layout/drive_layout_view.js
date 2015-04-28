/**
 * RequireJS module that defines the view: drive->layout.
 */

define([ 'app', 'apps/config/marionette/regions/fadein', 'tpl!apps/drive/layout/templates/drive-layout', 'tpl!apps/drive/layout/templates/tab-list',
		'tpl!apps/drive/layout/templates/tab-link' ], function(Lvl, FadeInRegion, LayoutTpl, TabListTpl, TabLinkTpl) {
	Lvl.module('DriveApp.Layout.View', function(View, Lvl, Backbone, Marionette, $, _) {
		View.TabLink = Marionette.ItemView.extend({
			tagName : 'li',
			template : TabLinkTpl,
			onRender : function() {
				if (this.model.selected) {
					this.$el.addClass('active');
				}
			}
		});
		View.TabList = Marionette.CompositeView.extend({
			template : TabListTpl,
			itemView : View.TabLink,
			appendHtml : function(driveView, itemView) {
				driveView.$('ul').append(itemView.el);
			}
		});
		View.Layout = Marionette.Layout.extend({
			template : LayoutTpl,
			regions : {
				tabList : '#section-tab-list',
				tabContent : FadeInRegion.extend({
					el : '#section-tab-content'
				})
			},
			initialize : function(options) {
				this.navLinks = options.navigation;				
			},
			onRender : function(options) {
				this.tabList.show(new View.TabList({
					collection : options.navLinks
				}));
			}
		});
	});
	return Lvl.DriveApp.Layout.View;
});