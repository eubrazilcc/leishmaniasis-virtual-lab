/**
 * RequireJS module that defines the view: analysis->run_workflow.
 */

define([ 'app', 'tpl!apps/analysis/submit/tpls/analysis_submit_pipeline', 'tpl!apps/analysis/submit/tpls/parameters',
		'tpl!apps/analysis/submit/tpls/parameter', 'apps/config/marionette/configuration', 'entities/wf_params', 'backbone.syphon', 'bootstrap3-typeahead' ],
		function(Lvl, SubmitPipelineTpl, ParametersTpl, ParamTpl, Configuration, ParamsEntity) {
			Lvl.module('AnalysisApp.Submit.View', function(View, Lvl, Backbone, Marionette, $, _) {
				'use strict';
				var config = new Configuration();
				View.ParamItem = Marionette.ItemView.extend({
					tagName : 'div',
					template : ParamTpl,
					onRender : function() {
						this.$el.addClass('form-group');
						if (this.model.get('name') === 'HTTPGet-RequestHeaders') {
							this.$el.addClass('hidden');
						} else if (this.model.get('name') === 'SequenceURL') {
							this.$el.find('input.form-control').attr({
								'data-provide' : 'typeahead',
								'autocomplete' : 'off'
							});
							this.$el.find('input[data-provide="typeahead"]').typeahead({
								delay : 200,
								source : function(query, process) {
									return $.ajax({
										type : 'GET',
										dataType : 'json',
										crossDomain : true,
										url : config.get('service', '') + '/datasets/objects/~/' + query + '/typeahead',
										headers : config.authorizationHeader()
									}).done(function(data) {
										return process(data);
									});
								}
							});
						}
					},
					onDestroy : function() {
						this.$el.find('input[data-provide="typeahead"]').typeahead('destroy');
					}
				});
				View.Parameters = Marionette.CompositeView.extend({
					template : ParametersTpl,
					childView : View.ParamItem,
					childViewContainer : 'fieldset',
					onRender : function() {
						this.collection.fetch({
							reset : true
						});
					}
				});
				View.Content = Marionette.LayoutView.extend({
					template : SubmitPipelineTpl,
					regions : {
						parametersRegion : '#lvl-wf-params'
					},
					initialize : function(options) {
						this.workflowId = options.workflowId;
						this.parameters = new ParamsEntity.WorkflowParametersCollection({
							oauth2_token : config.authorizationToken(),
							workflowId : this.workflowId
						});
						this.listenTo(this.parameters, 'reset', this.startTypeahead);
					},
					events : {
						'click button#submit-btn' : 'submitWorkflow'
					},
					submitWorkflow : function(e) {
						e.preventDefault();
						var formData = Backbone.Syphon.serialize(this);
						var self = this;
						var stableVersion = 0;
						switch (self.workflowId) {
						case 'workflows-eubcc-nj_pipeline-1.0':
							stableVersion = 902;
							break;
						default:
							stableVersion = 0;
							break;
						}
						var requestData = {
							'id' : null,
							'workflowId' : self.workflowId,
							'version' : stableVersion,
							'invocationId' : null,
							'parameters' : {
								'parameters' : []
							}
						};
						this.parameters.each(function(param) {
							var value = '';
							switch (param.get('name')) {
							case 'HTTPGet-RequestHeaders':
								value = 'Authorization: Bearer ' + config.authorizationToken()
								break;
							case 'SequenceURL':
								value = config.get('service') + '/datasets/objects/~/' + encodeURIComponent(formData[param.get('name')]) + '/download'
								break;
							default:
								value = formData[param.get('name')];
								break;
							}
							requestData.parameters.parameters.push({
								'name' : param.get('name'),
								'value' : value
							});
						});
						// submit request to LVL server
						$('#submit-btn').attr('disabled', 'disabled');
						var jqxhr = $.ajax({
							type : 'POST',
							contentType : 'application/json',
							crossDomain : true,
							url : config.get('service', '') + '/pipelines/runs/~',
							data : JSON.stringify(requestData),
							headers : config.authorizationHeader()
						}).done(
								function(data, textStatus, request) {
									self.trigger('destroy');
									require([ 'common/growl' ], function(createGrowl) {
										var anchor = $('<a>', {
											href : request.getResponseHeader('Location')
										})[0];
										var id = anchor.pathname.substring(anchor.pathname.lastIndexOf('/') + 1);
										createGrowl('Pipeline submitted: <a href="/#analysis/runs/' + id
												+ '"><i class="fa fa-arrow-circle-right fa-fw"></i> show</a>', false);
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
					},
					onBeforeShow : function() {
						this.showChildView('parametersRegion', new View.Parameters({
							collection : this.parameters
						}));
					}
				});
			});
			return Lvl.AnalysisApp.Submit.View;
		});