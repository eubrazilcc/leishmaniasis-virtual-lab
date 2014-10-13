/**
 * RequireJS module that defines the view: files->layout.
 */

define([ 'app', 'apps/config/marionette/regions/fadein', 'tpl!apps/files/layout/templates/files-layout', 'tpl!apps/files/layout/templates/tab-list',
		'tpl!apps/files/layout/templates/tab-link' ], function(Lvl, FadeInRegion, LayoutTpl, TabListTpl, TabLinkTpl) {
	Lvl.module('FilesApp.Layout.View', function(View, Lvl, Backbone, Marionette, $, _) {
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
			appendHtml : function(filesView, itemView) {
				filesView.$('ul').append(itemView.el);
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
	return Lvl.FilesApp.Layout.View;
});