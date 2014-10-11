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
					'invocationId' : null,
					'parameters' : {
						'parameters' : {
							'input_file' : [ {
								'key' : 'Source',
								'value' : formData.fasta_select
							} ],
							'dnapars' : [ {
								'key' : 'Number of data sets',
								'value' : formData.data_sets_input
							}, {
								'key' : 'Outgroup root',
								'value' : formData.outgroup_input
							} ],
							'seqboot' : [ {
								'key' : 'Number of replicates',
								'value' : formData.replicates_input
							}, {
								'key' : 'Random number seed',
								'value' : formData.seed_input
							} ]
						}
					},
					'submitter' : null,
					'submitted' : null,
					'links' : null
				};

				// TODO
				console.log('Submitting: ' + JSON.stringify(requestData));
				// TODO

				// submit request to LVL server
				$('#submit-btn').attr('disabled', 'disabled');
				var jqxhr = $.ajax({
					type : 'POST',
					contentType : 'application/json',
					crossDomain : true,
					url : config.get('service', '') + '/pipeline_runs',
					data : JSON.stringify(requestData),
					headers : config.authorizationHeader()
				}).done(function() {
					self.trigger('close');
				}).fail(function() {
					self.trigger('close');
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