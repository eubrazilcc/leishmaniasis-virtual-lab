/**
 * RequireJS module that defines the view: documentation->show.
 */

define([ 'app', 'tpl!apps/documentation/show/templates/documentation' ], function(Lvl, DocumentationTpl) {
	Lvl.module('DocumentationApp.Show.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		View.Content = Marionette.ItemView.extend({
			template : DocumentationTpl,
			initialize : function(options) {
				this.section = (options.section || 'documentation').toLowerCase();
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
					if ($(this).find('a').attr('href') === '/#doc/' + _self.section) {
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
			},
			events : {
				'show.bs.modal #modalVideoViewer' : 'showVideoViewer'
			},
			showVideoViewer : function(e) {
				var caller = $(e.relatedTarget);
				var data = caller.data('whatever');
				var modal = $('#modalVideoViewer');
				modal.find('.modal-title').text(data.title);
				modal.find('.modal-body div iframe').attr('src', data.url);
			}
		});
	});
	return Lvl.DocumentationApp.Show.View;
});