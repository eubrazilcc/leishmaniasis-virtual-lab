/**
 * RequireJS module that defines the view: analysis->text_viewer.
 */

define([ 'app', 'tpl!apps/analysis/text_viewer/templates/analysis_text_viewer', 'apps/config/marionette/configuration', 'pace' ], function(Lvl, TreeViewerTpl,
		Configuration, pace) {
	Lvl.module('AnalysisApp.TreeViewer.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		View.Content = Marionette.ItemView.extend({
			template : TreeViewerTpl,
			initialize : function(options) {
				this.product = options.product;
			},
			onRender : function() {
				var self = this;
				pace.start();
				var jqxhr = $.ajax(
						{
							type : 'GET',
							dataType : 'text',
							crossDomain : true,
							url : config.get('service', '') + '/pipelines/runs/text_product/' + self.product.id + '/'
									+ btoa(unescape(encodeURIComponent(self.product.path))),
							headers : config.authorizationHeader()
						}).done(function(data) {
					self.$('#textCanvas').html('<pre>' + data + '</pre>');
				}).fail(function() {
					self.trigger('destroy');
					require([ 'qtip' ], function(qtip) {
						var message = $('<p />', {
							text : 'Failed to load text product from the LVL service.'
						}), ok = $('<button />', {
							text : 'Close',
							'class' : 'full'
						});
						$('#alert').qtip({
							content : {
								text : message.add(ok),
								title : {
									text : 'Error',
									button : true
								}
							},
							position : {
								my : 'center',
								at : 'center',
								target : $(window)
							},
							show : {
								ready : true,
								modal : {
									on : true,
									blur : false
								}
							},
							hide : false,
							style : 'qtip-bootstrap dialogue',
							events : {
								render : function(event, api) {
									$('button', api.elements.content).click(function() {
										api.hide();
									});
								},
								hide : function(event, api) {
									api.destroy();
								}
							}
						});
					});
				});
			},
			onDestroy : function() {
				pace.stop();
			}
		});
	});
	return Lvl.AnalysisApp.TreeViewer.View;
});