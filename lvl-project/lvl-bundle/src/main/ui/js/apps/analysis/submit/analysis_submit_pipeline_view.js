/**
 * RequireJS module that defines the view: analysis->run_workflow.
 */

define([ 'app', 'tpl!apps/analysis/submit/tpls/analysis_submit_pipeline', 'tpl!apps/analysis/submit/tpls/parameters',
		'tpl!apps/analysis/submit/tpls/parameter', 'entities/wf_params', 'pace', 'backbone.syphon', 'bootstrap3-typeahead' ], function(Lvl, SubmitPipelineTpl,
		ParametersTpl, ParamTpl, ParamsEntity, pace) {
	Lvl.module('AnalysisApp.Submit.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
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
								url : Lvl.config.get('service.url') + '/datasets/objects/~/' + query + '/typeahead',
								headers : Lvl.config.authorizationHeader()
							}).done(function(data) {
								return process(data);
							});
						}
					});
				} else if (this.model.get('option_0') !== undefined) {
					var _self = this;
					var options = _.map(_.filter(_.keys(this.model.toJSON()), function(key) {
						return key.match(/^option_\d+/);
					}), function(opt) {
						var tokens = _self.model.get(opt).split('|');
						return {
							id : tokens[0],
							name : tokens[0] + ' (' + tokens[1] + ')'
						};
						return _self.model.get(opt);
					});
					this.$el.find('input.form-control').attr({
						'data-provide' : 'typeahead',
						'autocomplete' : 'off'
					});
					var input = this.$el.find('input[data-provide="typeahead"]');
					input.typeahead({
						source : options,
						autoSelect : true
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
				pace.restart();
				this.collection.fetch({
					reset : true,
					beforeSend : function(xhr) {
						xhr.setRequestHeader('Content-Type', 'application/json');
					},
					data : JSON.stringify(this.collection.wfOpts),
					type : 'POST'				
				}).always(function() {
					pace.stop();
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
				this.wfConf = options.wfConf;
				this.parameters = new ParamsEntity.WorkflowParametersCollection({
					oauth2_token : Lvl.config.authorizationToken(),
					workflowId : this.workflowId,
					versionId : this.wfConf.get('stable'),
					wfOpts : this.wfConf.get('parameters')
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
				var requestData = {
					'id' : null,
					'workflowId' : self.workflowId,
					'version' : self.parameters.versionId || 0,
					'invocationId' : null,
					'parameters' : {
						'parameters' : []
					}
				};
				this.parameters.each(function(param) {
					var value = '';
					switch (param.get('name')) {
					case 'HTTPGet-RequestHeaders':
						value = 'Authorization: Bearer ' + Lvl.config.authorizationToken()
						break;
					case 'SequenceURL':
						value = Lvl.config.get('service.url') + '/datasets/objects/~/' + encodeURIComponent(formData[param.get('name')]) + '/download'
						break;
					default:
						var inVal = formData[param.get('name')];
						if (inVal.match(/^\d+\s+\([\w\._-]+\)/)) {
							value = inVal.split(' ')[0];
						} else {
							value = inVal;
						}
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
					url : Lvl.config.get('service.url') + '/pipelines/runs/~',
					data : JSON.stringify(requestData),
					headers : Lvl.config.authorizationHeader()
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
					require([ 'common/alert' ], function(alertDialog) {
						alertDialog('Error', 'Failed to submit molecular pipeline to the LVL service.');
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