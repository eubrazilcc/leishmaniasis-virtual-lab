/**
 * RequireJS module that defines the view: analysis->run_workflow.
 */

define([ 'app', 'tpl!apps/analysis/upload/templates/analysis_upload_dataset', 'apps/config/marionette/configuration', 'backbone.syphon' ], function(Lvl,
		UploadDatasetTpl, Configuration) {
	Lvl.module('AnalysisApp.Upload.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		View.Content = Marionette.ItemView.extend({
			template : UploadDatasetTpl,
			initialize : function(options) {
				this.datasets = options.datasets
			},
			events : {
				'click button#upload-btn' : 'uploadDataset'
			},
			uploadDataset : function(e) {
				e.preventDefault();
				var formData = Backbone.Syphon.serialize(this);
				var self = this;
				var requestData = {
					'created' : null,
					'description' : formData.description_input,
					'id' : null,
					'links' : null,
					'name' : formData.dataset_select
				};
				// submit request to LVL server
				$('#submit-btn').attr('disabled', 'disabled');
				var jqxhr = $.ajax({
					type : 'POST',
					contentType : 'application/json',
					crossDomain : true,
					url : config.get('service', '') + '/pipelines_data',
					data : JSON.stringify(requestData),
					headers : config.authorizationHeader()
				}).done(function() {
					self.datasets.trigger('analysis:dataset:added');
					self.trigger('close');
				}).fail(function() {
					self.trigger('close');
					require([ 'qtip' ], function(qtip) {
						var message = $('<p />', {
							text : 'Failed to upload dataset to the LVL service.'
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
			}
		});
	});
	return Lvl.AnalysisApp.Upload.View;
});