/**
 * RequireJS module that defines the view: drive->upload_dataset.
 */

define([ 'app', 'tpl!apps/drive/upload/tpls/ds_up', 'pace', 'bootstrapvalidator', 'backbone.syphon' ], function(Lvl, UploadDatasetTpl, pace) {
	Lvl.module('DriveApp.Upload.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		View.Content = Marionette.ItemView.extend({
			template : UploadDatasetTpl,
			events : {
				'click button#upload-btn' : 'uploadDataset'
			},
			initialize : function() {
				this.listenTo(this.model, 'change', this.modelChanged);
			},
			modelChanged : function(model) {
				var _self = this;
				if (model.get('overwrite') === false) {
					_self.saveModel();
				} else {
					require([ 'common/confirm' ], function(confirmDialog) {
						confirmDialog('Dataset overwriting',
								'This action will overwrite/replace an existing dataset. Are you sure?<p>Dataset: <span class="text-danger">'
										+ model.get('effective_fname') + '</span></p>', function() {
									_self.saveModel();
								}, {
									btn_text : 'Replace'
								});
					});
					_self.trigger('destroy');
				}
			},
			saveModel : function() {
				// submit request to LVL server
				pace.restart();
				var _self = this;
				var jqxhr = $.ajax({
					type : 'POST',
					processData : false,
					contentType : false,
					crossDomain : true,
					url : Lvl.config.get('service.url') + '/datasets/objects/~/upload',
					data : _self.model.get('request'),
					headers : Lvl.config.authorizationHeader()
				}).always(function() {
					pace.stop();
				}).done(function(data, textStatus, request) {
					_self.trigger('destroy');
					require([ 'common/growl' ], function(createGrowl) {
						var anchor = $('<a>', {
							href : request.getResponseHeader('Location')
						})[0];
						var filename = anchor.pathname.substring(anchor.pathname.lastIndexOf('/') + 1);
						createGrowl('New dataset created', filename, false);
						Lvl.vent.trigger('datasets:file:uploaded');
					});
				}).fail(function() {
					_self.trigger('destroy');
					require([ 'common/alert' ], function(alertDialog) {
						alertDialog('Error', 'Failed to save dataset.');
					});
				});
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
				_self.$('#upload-btn').prop('disabled', true);
				$.ajax({
					type : 'GET',
					dataType : 'json',
					crossDomain : true,
					url : Lvl.config.get('service.url') + '/datasets/objects/~/' + encodeURIComponent(eFname) + '/typeahead',
					headers : Lvl.config.authorizationHeader()
				}).always(function() {
					pace.stop();
					var form = $('#dsUpForm');
					form.bootstrapValidator('resetForm', true);
					form.bootstrapValidator('disableSubmitButtons', true);
				}).done(function(data) {
					_self.model.set({
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
			onShow : function() {
				var _self = this;
				$('#dsUpForm').on('init.form.bv', function(e, data) {
					_self.$('#upload-btn').prop('disabled', true);
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
						_self.$('#upload-btn').prop('disabled', false);
					} else {
						_self.$('#upload-btn').prop('disabled', true);
					}
				});
			}
		});
	});
	return Lvl.DriveApp.Upload.View;
});