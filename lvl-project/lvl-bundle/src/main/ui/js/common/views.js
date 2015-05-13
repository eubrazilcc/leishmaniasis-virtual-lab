/**
 * RequireJS module that defines the views that are common to all the application and sub-applications.
 * TODO : check whether this is still needed. Otherwise, remove.
 */

define([ 'app', 'tpl!common/tpls/loading', 'jquery.spin' ], function(Lvl, LoadingTpl, ErrorDialogTpl) {
	Lvl.module('Common.Views', function(Views, Lvl, Backbone, Marionette, $, _) {
		Views.Loading = Marionette.ItemView.extend({
			template : LoadingTpl,
			onShow : function() {
				var docHeight = $(document).height();
				$('body').append("<div id='lvl-loading-overlay'></div>");
				$('#lvl-loading-overlay').height(docHeight).css({
					'opacity' : 0.4,
					'position' : 'absolute',
					'top' : 0,
					'left' : 0,
					'background-color' : 'black',
					'width' : '100%',
					'z-index' : 5000
				});
				$('body').append("<div style='display: none;' id='lvl-loading-spinner'></div>");
				var opts = {
					lines : 13,
					length : 20,
					width : 10,
					radius : 30,
					corners : 1,
					rotate : 0,
					direction : 1,
					color : '#000',
					speed : 1,
					trail : 60,
					shadow : false,
					hwaccel : false,
					className : 'spinner',
					zIndex : 2e9,
					top : '50%',
					left : '50%'
				};
				$('#lvl-loading-spinner').spin(opts);
				$('#lvl-loading-spinner').fadeToggle('fast');
			},
			onBeforeClose : function() {
				$.when($('#lvl-loading-spinner').spin(false)).then(function() {
					$('#lvl-loading-overlay').animate({
						'opacity' : '0',
					}, {
						'duration' : 'fast',
						'complete' : function() {
							$('#lvl-loading-spinner').remove();
							$('#lvl-loading-overlay').remove();
						}
					});
				});
			},
			onDestroy : function() {
				$('#lvl-loading-spinner').remove();
				$('#lvl-loading-overlay').remove();
			}
		});
	});
	return Lvl.Common.Views;
});