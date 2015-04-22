/**
 * RequireJS module that defines the view: open->layout.
 */

define([ 'app', 'tpl!apps/open/layout/tpls/open-layout', 'tpl!apps/open/layout/tpls/subsection', 'tpl!apps/open/layout/tpls/nav-list',
		'tpl!apps/open/layout/tpls/nav-list-item', 'tpl!apps/open/layout/tpls/event-list', 'tpl!apps/open/layout/tpls/event-list-item',
		'apps/open/layout/entities/toc' ], function(Lvl, LayoutTpl, SubsectionTpl, NavListTpl, NavItemTpl, EventListTpl, EventItemTpl, TocEntity) {
	Lvl.module('OpenContent.Layout.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		View.NavItem = Marionette.ItemView.extend({
			tagName : 'li',
			template : NavItemTpl,
			onRender : function() {
				if (this.model.selected) {
					this.$el.addClass('active');
				}
			}
		});
		View.NavList = Marionette.CompositeView.extend({
			tagName : 'ul',
			template : NavListTpl,
			childView : View.NavItem
		});
		View.EventItem = Marionette.ItemView.extend({
			tagName : 'li',
			template : EventItemTpl
		});
		View.EventList = Marionette.CompositeView.extend({
			template : EventListTpl,
			childView : View.EventItem,
			childViewContainer : 'ul',
			onRender : function() {
				this.collection.fetch({
					reset : true
				});
			},
		});		
		View.SubSection = Marionette.ItemView.extend({
			template : SubsectionTpl,
			templateHelpers : function() {
				return {
					name : this.model.get('sub-section-name'),
					application : this.model.get('application'),
					section : this.model.get('sub-section'),
					content : this.model.get('sub-section-content')
				}
			}
		});
		View.Layout = Marionette.LayoutView.extend({
			template : LayoutTpl,
			templateHelpers : function() {
				return {
					name : this.model.get('name'),
					application : this.model.get('application'),
					section : this.model.get('section')
				}
			},
			regions : {
				tocNavigationBar : '#lvl-toc-nav',
				mainSectionContent : '#lvl-main-section-content',
				eventList : '#lvl-events-list'
			},
			initialize : function(options) {
				this.mainSectionHtml = options.mainSectionHtml;
				this.subSectionsHtml = options.subSectionsHtml;
				$(window).on('resize', this.setupAffix);
			},
			onDestroy : function() {
				$(window).off('resize', this.setupAffix);
			},
			setupAffix : function() {
				var _self = this;
				require([ 'domReady' ], function(domReady) {
					domReady(function() {
						$('div#lvl-toc-nav').affix({
							offset : {
								top : $('div#lvl-toc-nav').offset().top,
								bottom : $('div#footer-region').outerHeight(true)
							}
						});
					});
				});
			},
			onBeforeShow : function() {
				var section = this.model.get('section');				
				// main section
				var MainSectionView = Marionette.ItemView.extend({
					template : _.template(this.mainSectionHtml)
				});
				this.showChildView('mainSectionContent', new MainSectionView());
				// sub-sections
				for (var i = 0; i < this.subSectionsHtml.length; i++) {					
					this.addRegion('lvl-subsection_' + i, "#lvl-subsection_" + i);
					this.getRegion('lvl-subsection_' + i).show(new View.SubSection({
						model : new Backbone.Model({
							'sub-section-name' : 'Name',
							'application' : this.model.get('application'),
							'sub-section' : 'section',
							'sub-section-content' : '123'
						})
					}));
				}
				
				
				// TODO : end here //  this.subSectionViews[i] // (section || 'unknown').toLowerCase()
				
				
				
				// table of content				
				var toc = new TocEntity.TocCollection([ {
					id : 0,
					application : this.model.get('application'),
					section : section,
					text : this.model.get('name')
				} ]);				
				var tocItemToSelect = toc.find(function(tocItem) {
					return tocItem.get('section') === section;
				});
				tocItemToSelect.select();
				toc.trigger('reset');
				this.showChildView('tocNavigationBar', new View.NavList({
					collection : toc
				}));
				
				
				// events list
				this.showChildView('eventList', new View.EventList({
					collection : this.model.get('agenda')
				}));				
			},
			onShow : function() {
				var _self = this, application = _self.model.get('application'), section = _self.model.get('section');
				$('div#lvl-toc-nav > ul > li').each(function() {
					if ($(this).find('a').attr('href') === '/#' + application + '/' + section) {
						$(this).addClass('active');
					} else {
						$(this).removeClass('active');
					}
				});
				var emSize = parseFloat($('section#' + section).css('font-size'));
				$('html, body').animate({
					scrollTop : $('section#' + section).offset().top - (_self.$el.offset().top + 6.5 * emSize)
				}, 700);
			},
			onDomRefresh : function() {
				this.setupAffix();
			}
		});
	});
	return Lvl.OpenContent.Layout.View;
});