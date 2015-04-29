/**
 * RequireJS module that defines the view: analysis->run_workflow.
 */

define([ 'app', 'tpl!apps/analysis/submit/templates/analysis_submit_pipeline', 'apps/config/marionette/configuration', 'backbone.syphon' ], function(Lvl,
		SubmitPipelineTpl, Configuration) {
	Lvl.module('AnalysisApp.Submit.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		View.Content = Marionette.ItemView.extend({
			template : SubmitPipelineTpl,
			initialize : function(options) {
				this.workflowId = options.workflowId
			},
			events : {
				'click button#submit-btn' : 'submitWorkflow'
			},
			submitWorkflow : function(e) {
				e.preventDefault();
				var formData = Backbone.Syphon.serialize(this);
				var self = this;
				var requestData = {
					'id' : null,
					'workflowId' : self.workflowId,
					'version' : '902',
					'invocationId' : null,
					'parameters' : {
						'parameters' : [ {
							'name' : 'SequenceURL',
							'value' : config.get('service') + '/datasets/objects/~/' + encodeURIComponent(formData.fasta_select) + '/download'
						}, {
							'name' : 'HTTPGet-RequestHeaders',
							'value' : 'Authorization: Bearer ' + config.authorizationToken()
						}, {
							'name' : 'Align',
							'value' : formData.align_input
						}, {
							'name' : 'No. of Bootstrap Replications',
							'value' : formData.replicates_input
						} ]
					},
					'submitter' : null,
					'submitted' : null,
					'links' : null
				};
				// submit request to LVL server
				$('#submit-btn').attr('disabled', 'disabled');
				var jqxhr = $.ajax({
					type : 'POST',
					contentType : 'application/json',
					crossDomain : true,
					url : config.get('service', '') + '/pipelines/runs/~',
					data : JSON.stringify(requestData),
					headers : config.authorizationHeader()
				}).done(function(data, textStatus, request) {
					self.trigger('destroy');
					require([ 'common/growl' ], function(createGrowl) {
						var anchor = $('<a>', {
							href : request.getResponseHeader('Location')
						})[0];
						var id = anchor.pathname.substring(anchor.pathname.lastIndexOf('/') + 1);
						createGrowl('Pipeline submitted: <a href="/#analysis/runs/' + id + '"><i class="fa fa-arrow-circle-right fa-fw"></i> show</a>', false);
					});
				}).fail(function() {
					self.trigger('destroy');
					require([ 'qtip' ], function(qtip) {
						var message = $('<p />', {
							text : 'Failed to submit molecular pipeline to the LVL service.'
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
	return Lvl.AnalysisApp.Submit.View;
});