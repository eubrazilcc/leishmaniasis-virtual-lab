/**
 * RequireJS module that defines the view: analysis->layout.
 */

define([ 'app', 'apps/config/marionette/regions/fadein', 'tpl!apps/analysis/layout/templates/analysis-layout', 'tpl!apps/analysis/layout/templates/tab-list',
		'tpl!apps/analysis/layout/templates/tab-link' ], function(Lvl, FadeInRegion, LayoutTpl, TabListTpl, TabLinkTpl) {
	Lvl.module('AnalysisApp.Layout.View', function(View, Lvl, Backbone, Marionette, $, _) {
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
			onRender : function(options) {
				this.tabList.show(new View.TabList({
					collection : options.navLinks
				}));
			}
		});
	});
	return Lvl.AnalysisApp.Layout.View;
});