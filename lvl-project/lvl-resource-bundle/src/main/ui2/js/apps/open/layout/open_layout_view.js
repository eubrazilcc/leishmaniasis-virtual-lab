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
					application : this.model.get('subsection-application'),
					section : this.model.get('subsection-section'),
					name : this.model.get('subsection-name'),
					content : this.model.get('subsection-content')
				}
			}
		});
		View.Layout = Marionette.LayoutView.extend({
			template : LayoutTpl,
			templateHelpers : function() {
				return {
					name : this.model.get('mainSection').get('name'),
					application : this.model.get('mainSection').get('section')
				}
			},
			regions : {
				tocNavigationBar : '#lvl-toc-nav',
				mainSectionContent : '#lvl-main-section-content',
				eventList : '#lvl-events-list'
			},
			initialize : function(options) {
				this.activeSection = options.activeSection;
				this.mainSection = options.mainSection;
				this.subSections = options.subSections;
				this.agenda = options.agenda;
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
				// start the table of content (ToC)
				var toc = new TocEntity.TocCollection([ {
					id : 0,
					application : this.model.get('mainSection').get('section'),
					section : this.model.get('mainSection').get('section'),
					text : this.model.get('mainSection').get('name')
				} ]);
				// create main section
				var MainSectionView = Marionette.ItemView.extend({
					template : _.template(this.model.get('mainSection').get('htmlContent'))
				});
				this.showChildView('mainSectionContent', new MainSectionView());
				// create sub-sections and add then to the ToC
				for (var i = 0; i < this.model.get('subSections').length; i++) {
					this.addRegion('lvl-subsection_' + i, "#lvl-subsection_" + i);
					var subsectionView = new View.SubSection({
						model : new Backbone.Model({
							'subsection-application' : this.model.get('mainSection').get('section'),
							'subsection-name' : this.model.get('subSections').at(i).get('name'),
							'subsection-section' : this.model.get('subSections').at(i).get('section'),
							'subsection-content' : this.model.get('subSections').at(i).get('htmlContent')
									+ (i < this.model.get('subSections').length - 1 ? '<hr>' : '')
						})
					});
					if (this.model.get('subSections').at(i).get('events')) {
						subsectionView.delegateEvents(this.model.get('subSections').at(i).get('events'));
					}
					this.getRegion('lvl-subsection_' + i).show(subsectionView);
					toc.add([ {
						id : i + 1,
						application : this.model.get('mainSection').get('section'),
						section : this.model.get('subSections').at(i).get('section'),
						text : this.model.get('subSections').at(i).get('name')
					} ]);
				}
				// find which is the active section
				var activeSection = this.model.get('activeSection') || this.model.get('mainSection').get('section');
				var tocItemToSelect = toc.find(function(tocItem) {
					return tocItem.get('section') === activeSection;
				});
				tocItemToSelect.select();
				toc.trigger('reset');
				this.showChildView('tocNavigationBar', new View.NavList({
					collection : toc
				}));
				// events list (agenda)
				this.showChildView('eventList', new View.EventList({
					collection : this.model.get('agenda')
				}));
			},
			onShow : function() {
				var _self = this, application = _self.model.get('mainSection').get('section'), activeSection = (_self.model.get('activeSection') || _self.model
						.get('mainSection').get('section'));
				$('div#lvl-toc-nav > ul > li').each(function() {
					if ($(this).find('a').attr('href') === '/#' + application + '/' + activeSection) {
						$(this).addClass('active');
					} else {
						$(this).removeClass('active');
					}
				});
				var emSize = parseFloat($('section#' + activeSection).css('font-size'));
				$('html, body').animate({
					scrollTop : $('section#' + activeSection).offset().top - (_self.$el.offset().top + 6.5 * emSize)
				}, 700);
			},
			onDomRefresh : function() {
				this.setupAffix();
			}
		});
	});
	return Lvl.OpenContent.Layout.View;
});