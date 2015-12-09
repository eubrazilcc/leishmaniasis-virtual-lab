/**
 * RequireJS module that defines the view: maps->layout.
 */

define([ 'app', 'apps/config/marionette/regions/fadein', 'tpl!apps/maps/layout/tpls/maps-layout', 'tpl!apps/maps/layout/tpls/tab-list',
		'tpl!apps/maps/layout/tpls/tab-link' ], function(Lvl, FadeInRegion, LayoutTpl, TabListTpl, TabLinkTpl) {
	Lvl.module('MapsApp.Layout.View', function(View, Lvl, Backbone, Marionette, $, _) {
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
			childViewContainer : 'ul',
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
				this.navLinks = options.navigation;// subscribe to events
				$(document).on('keyup', this.handleEscKeyUpEvent);
			},
			onDestroy : function() {
				// unsubscribe from events
				$(document).off('keyup', this.handleEscKeyUpEvent);
			},
			onBeforeShow : function() {
				this.showChildView('tabList', new View.TabList({
					collection : this.navLinks
				}));
			},
			events : {
				'click button#lvl-toggle-toolbar-btn' : 'toggleToolbar',
				'click a#lvl-collapse-toolbar-btn' : 'closeToolbar'
			},
			toggleToolbar : function(e) {
				e.preventDefault();
				var toolbar = $('#lvl-floating-menu');
				if (toolbar.is(':visible')) {
					toolbar.hide('fast');
				} else {
					toolbar.show('fast');
				}
			},
			closeToolbar : function(e) {
				e.preventDefault();
				$('#lvl-floating-menu').hide('fast');
			},
			handleEscKeyUpEvent : function(e) {
				var toolbar = $('#lvl-floating-menu');
				if (e.which == 27 && toolbar.is(':visible')) {
					toolbar.hide('fast');
				}
			}
		});
	});
	return Lvl.MapsApp.Layout.View;
});