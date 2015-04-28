/**
 * RequireJS module that defines the view: saved-items->layout.
 */

define([ 'app', 'apps/config/marionette/regions/fadein', 'tpl!apps/saved-items/layout/tpls/saved-items-layout', 'tpl!apps/saved-items/layout/tpls/tab-list',
		'tpl!apps/saved-items/layout/tpls/tab-link' ], function(Lvl, FadeInRegion, LayoutTpl, TabListTpl, TabLinkTpl) {
	Lvl.module('SavedItemsApp.Layout.View', function(View, Lvl, Backbone, Marionette, $, _) {
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
			childView : View.TabLink,
			childViewContainer : 'ul'
		});
		View.Layout = Marionette.LayoutView.extend({
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
			onBeforeShow : function() {
				this.showChildView('tabList', new View.TabList({
					collection : this.navLinks
				}));
			}
		});
	});
	return Lvl.SavedItemsApp.Layout.View;
});