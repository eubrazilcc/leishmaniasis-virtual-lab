/**
 * RequireJS module that defines the view: analysis->text_viewer.
 */

define([ 'app', 'tpl!apps/analysis/text_viewer/tpls/analysis_text_viewer', 'pace' ], function(Lvl, TreeViewerTpl, pace) {
	Lvl.module('AnalysisApp.TreeViewer.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		View.Content = Marionette.ItemView.extend({
			template : TreeViewerTpl,
			initialize : function(options) {
				this.product = options.product;
			},
			events : {
				'click a#btn-open-tree-viewer' : 'openTreeViewer'
			},
			openTreeViewer : function(e) {
				e.preventDefault();
				var self = this;
				self.trigger('destroy');
				Lvl.flash(self.product).navigate('viewer/tree', {
					trigger : true
				});				
			},
			onRender : function() {
				var self = this;
				pace.start();
				var jqxhr = $.ajax(
						{
							type : 'GET',
							dataType : 'text',
							crossDomain : true,
							url : Lvl.config.get('service', '') + '/pipelines/runs/text_product/~/' + self.product.id + '/'
									+ btoa(unescape(encodeURIComponent(self.product.path))),
							headers : Lvl.config.authorizationHeader()
						}).done(
						function(data) {
							self.$('#textCanvas').html('<pre>' + data + '</pre>');
							self.$('#btn-view-raw').attr(
									{
										'href' : Lvl.config.get('service', '') + '/pipelines/runs/text_product/~/' + self.product.id + '/'
												+ btoa(unescape(encodeURIComponent(self.product.path))) + '?' + Lvl.config.authorizationQuery()
									});
							self.$('#btn-download-raw').attr(
									{
										'href' : Lvl.config.get('service', '') + '/pipelines/runs/text_product/~/' + self.product.id + '/'
												+ btoa(unescape(encodeURIComponent(self.product.path))) + '?' + Lvl.config.authorizationQuery(),
										'download' : self.product.path
									});
							if (self.product.path.substr(self.product.path.length - '.nwk'.length, self.product.path.length) === '.nwk') {
								self.$('#btn-open-tree-viewer').removeClass('hidden');
							}
						}).fail(function() {
					self.trigger('destroy');
					require([ 'common/alert' ], function(alertDialog) {
						alertDialog('Error', 'Failed to load text product from the LVL service.');
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