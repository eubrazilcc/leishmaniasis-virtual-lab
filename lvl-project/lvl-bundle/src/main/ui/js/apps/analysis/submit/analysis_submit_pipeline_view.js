/**
 * RequireJS module that defines the view: analysis->run_workflow.
 */

define([ 'app', 'tpl!apps/analysis/submit/tpls/analysis_submit_pipeline', 'tpl!apps/analysis/submit/tpls/parameters',
		'tpl!apps/analysis/submit/tpls/parameter', 'tpl!apps/analysis/submit/tpls/parameter_upload', 'entities/wf_params', 
		'pace', 'backbone.syphon', 'bootstrapvalidator', 'bootstrap3-typeahead' ], function(Lvl, SubmitPipelineTpl,
		ParametersTpl, ParamTpl, ParamUploadTpl, ParamsEntity, pace) {
	Lvl.module('AnalysisApp.Submit.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		View.ParamItem = Marionette.ItemView.extend({
			tagName : 'div',
			template : ParamTpl,
			getTemplate: function() {
				return ( this.model.get('name') === 'SequenceURL' ) ? ParamUploadTpl : ParamTpl;
			},
			onRender : function() {
				this.$el.addClass('form-group');
				if (this.model.get('name') === 'HTTPGet-RequestHeaders') {
					this.$el.addClass('hidden');
				} else if (this.model.get('name') === 'SequenceURL') {
					this.$el.find('input.form-control').attr({
						'data-provide' : 'typeahead',
						'autocomplete' : 'off',
						'placeholder' : 'Start typing a filename to get suggestions'
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
					var options = _.sortBy(_.map(_.filter(_.keys(this.model.toJSON()), function(key) {
						return key.match(/^option_\d+/);
					}), function(opt) {
						var tokens = _self.model.get(opt).split('|');
						return {
							id : tokens[0],
							name : tokens[0] + ' (' + tokens[1] + ')'
						};
						return _self.model.get(opt);
					}), 'id');
					this.$el.find('input.form-control').attr({
						'data-provide' : 'typeahead',
						'autocomplete' : 'off'
					});
					var input = this.$el.find('input[data-provide="typeahead"]');
					input.val(options[0].name);
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
				this.uploadModel = new Backbone.Model({
					'request' : null,
					'effective_fname' : null,
					'overwrite' : true,
					'parameter_id' : null,
					defaults: {
						'overwrite' : true
					}
				});
				this.listenTo(this.parameters, 'reset', this.startTypeahead);
				this.listenTo(this.uploadModel, 'change', this.uploadModelChanged);
			},			
			ui : {
				paramsTab : '#tabList a[href="#params"]',
				uploadTab : '#tabList a[href="#upload"]',
				confirmUploadTab : '#tabList a[href="#confirm-upload"]',
				uploadBtn : 'button.upload-button',
				backToParamsBtn : 'button.back-to-params',
				uploadFormBtn : 'button.upload-form-button',
				confirmUploadBtn : 'button.confirm-upload-button',
				submitBtn : 'button#submit-btn'
			},
			events : {
				'click @ui.uploadBtn': 'switchToUploadView',
				'click @ui.backToParamsBtn' : 'switchToParamsView',
				'click @ui.uploadFormBtn' : 'uploadDataset',
				'click @ui.confirmUploadBtn' : 'saveUploadModel',
				'click @ui.submitBtn' : 'submitWorkflow'
			},
			switchToUploadView : function(e) {
				var targetParam = $( e.target ).attr('data-parameter-id');
				this.uploadModel.set({ parameter_id: targetParam }, { silent: true });				
				this.ui.submitBtn.attr('disabled', 'disabled');
				this.ui.uploadTab.tab('show');
			},
			switchToParamsView : function() {
				this.ui.submitBtn.prop('disabled', false);
				this.ui.paramsTab.tab('show');
				this.uploadModel.clear({ silent: true });
			},
			uploadDataset : function(e) {
				e.preventDefault();
				pace.restart();
				var formData = Backbone.Syphon.serialize(this);
				var request = new FormData();
				var dsBlob = new Blob([ JSON.stringify({
					'filename' : formData.filename_input,
					'metadata' : {
						'description' : formData.description_input,
						'target' : {}
					}
				}) ], {
					type : 'application/json'
				});
				var file = $('#file_input').get(0).files[0];
				var eFname = formData.filename_input || file.name;
				request.append('dataset', dsBlob);
				request.append('file', file);
				// check filename availability
				var _self = this;
				_self.$('.upload-form-button').prop('disabled', true);
				$.ajax({
					type : 'GET',
					dataType : 'json',
					crossDomain : true,
					url : Lvl.config.get('service.url') + '/datasets/objects/~/' + encodeURIComponent(eFname) + '/typeahead',
					headers : Lvl.config.authorizationHeader()
				}).always(function() {
					pace.stop();
					var form = $('#lvl-wf-upload-dataset');
					form.bootstrapValidator('resetForm', true);
					form.bootstrapValidator('disableSubmitButtons', true);
				}).done(function(data) {					
					_self.uploadModel.set({
						'request' : request,
						'effective_fname' : eFname,
						'overwrite' : _.contains(data, eFname)
					});
				}).fail(function() {
					_self.trigger('destroy');
					require([ 'common/alert' ], function(alertDialog) {
						alertDialog('Error', 'Failed to save dataset.');
					});
				});
			},
			uploadModelChanged : function(model) {				
				var _self = this;
				if (model.get('overwrite') === false) {
					_self.saveUploadModel();					
				} else {					
					_self.$("#uploadFilename").text(model.get('effective_fname'));
					_self.ui.confirmUploadTab.tab('show');					
				}
			},			
			saveUploadModel : function() {
				// submit request to LVL server
				pace.restart();
				var _self = this;
				var jqxhr = $.ajax({
					type : 'POST',
					processData : false,
					contentType : false,
					crossDomain : true,
					url : Lvl.config.get('service.url') + '/datasets/objects/~/upload',
					data : _self.uploadModel.get('request'),
					headers : Lvl.config.authorizationHeader()
				}).always(function() {
					pace.stop();
				}).done(function(data, textStatus, request) {
					var anchor = $('<a>', {
						href : request.getResponseHeader('Location')
					})[0];
					var filename = anchor.pathname.substring(anchor.pathname.lastIndexOf('/') + 1);
					_self.$('input#' + _self.uploadModel.get('parameter_id')).val(filename);					
					_self.uploadModel.clear({ silent: true });
					_self.ui.submitBtn.prop('disabled', false);
					_self.ui.paramsTab.tab('show');					
				}).fail(function() {
					_self.trigger('destroy');
					require([ 'common/alert' ], function(alertDialog) {
						alertDialog('Error', 'Failed to save dataset.');
					});
				});				
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
			},
			onShow : function() {
				var _self = this;
				$('#lvl-wf-upload-dataset').on('init.form.bv', function(e, data) {
					_self.$('.upload-form-button').prop('disabled', true);
				}).bootstrapValidator({
					fields : {
						'file_input' : {
							verbose : false,
							validators : {
								file : {
									extension : 'txt,xml,fas,fasta,fna,ffn,faa,frn',
									type : 'text/plain,text/xml',
									maxSize : 5 * 1024 * 1024,
									message : 'The selected file is not valid, it should be (txt,xml,fas,fasta,fna,ffn,faa,frn) and 5 MB at maximum.'
								}
							}
						}
					}
				}).on('success.field.bv', function(e, data) {
					var isValid = data.bv.isValid();
					if (isValid) {
						_self.$('.upload-form-button').prop('disabled', false);
					} else {
						_self.$('.upload-form-button').prop('disabled', true);
					}
				});
			}
		});
	});
	return Lvl.AnalysisApp.Submit.View;
});