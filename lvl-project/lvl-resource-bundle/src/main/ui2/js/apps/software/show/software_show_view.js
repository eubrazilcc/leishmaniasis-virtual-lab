/**
 * RequireJS module that defines the view: software->show.
 */

define([ 'app', 'tpl!apps/software/show/templates/software' ], function(Lvl, SoftwareTpl) {
	Lvl.module('SoftwareApp.Show.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		View.Content = Marionette.ItemView.extend({
			template : SoftwareTpl,
			initialize : function(options) {
				this.section = (options.section || 'software').toLowerCase();
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
			onShow : function() {
				var _self = this;
				$('div#lvl-toc-nav > ul > li').each(function() {
					if ($(this).find('a').attr('href') === '/#software/' + _self.section) {
						$(this).addClass('active');
					} else {
						$(this).removeClass('active');
					}
				});				
				var emSize = parseFloat($('section#' + _self.section).css('font-size'));				
				$('html, body').animate({
					scrollTop : $('section#' + _self.section).offset().top - (_self.$el.offset().top + 6.5 * emSize)
				}, 700);
			},
			onDomRefresh : function() {
				this.setupAffix();
			}
		});
	});
	return Lvl.SoftwareApp.Show.View;
});