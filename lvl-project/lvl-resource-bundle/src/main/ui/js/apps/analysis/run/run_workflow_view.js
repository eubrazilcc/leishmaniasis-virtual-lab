/**
 * RequireJS module that defines the view: analysis->run_workflow.
 */

define([ 'app', 'tpl!apps/analysis/run/templates/analysis_run_workflow', 'apps/config/marionette/configuration', 'backbone.syphon' ], function(Lvl,
		RunWorkflowTpl, Configuration) {
	Lvl.module('AnalysisApp.Run.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		View.Content = Marionette.ItemView.extend({
			template : RunWorkflowTpl,
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
						'input_file' : [ {
							'left' : 'Source',
							'right' : formData.fasta_select
						} ],
						'seqboot' : [ {
							'left' : 'Number of replicates',
							'right' : formData.replicates_input
						}, {
							'left' : 'Random number seed',
							'right' : formData.seed_input
						} ],
						'dnapars' : [ {
							'left' : 'Number of data sets',
							'right' : formData.data_sets_input
						}, {
							'left' : 'Outgroup root',
							'right' : formData.outgroup_input
						} ]
					},
					'submitter' : null,
					'submitted' : null
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
	return Lvl.AnalysisApp.Run.View;
});