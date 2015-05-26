/**
 * RequireJS module that defines the view: drive->upload_dataset.
 */

define([ 'app', 'tpl!apps/drive/upload/tpls/ds_up', 'bootstrapvalidator', 'backbone.syphon' ], function(Lvl, UploadDatasetTpl) {
	Lvl.module('DriveApp.Upload.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		View.Content = Marionette.ItemView.extend({
			template : UploadDatasetTpl,			
			events : {
				'click button#upload-btn' : 'uploadDataset'
			},
			uploadDataset : function(e) {
				e.preventDefault();
				var _self = this;
				_self.$('#upload-btn').prop('disabled', true);
				var formData = Backbone.Syphon.serialize(this);
				
				
				
				
				/* TODO var requestData = {
					'filename' : formData.filename_input,
					'metadata' : {
						'description' : formData.description_input,
						'target' : {
							'collection' : this.data_source,
							'compression' : formData.compression_select,
							'filter' : formData.filter_select,
							'ids' : _.pluck(this.collection.toJSON(), 'id'),
							'type' : 'sequence'
						}
					}
				};
				// submit request to LVL server
				var self = this;
				$('#export-btn').attr('disabled', 'disabled');				
				var jqxhr = $.ajax({
					type : 'POST',
					contentType : 'application/json',
					crossDomain : true,
					url : Lvl.config.get('service', '') + '/datasets/objects/~',
					data : JSON.stringify(requestData),
					headers : Lvl.config.authorizationHeader()
				}).done(
						function(data, textStatus, request) {
							self.trigger('destroy');
							require([ 'common/growl' ], function(createGrowl) {
								var anchor = $('<a>', {
									href : request.getResponseHeader('Location')
								})[0];
								var filename = anchor.pathname.substring(anchor.pathname.lastIndexOf('/') + 1);
								createGrowl('New dataset created', filename
										+ ' <a href="/#drive/datasets"><i class="fa fa-arrow-circle-right fa-fw"></i> datasets</a>', false);
							});
						}).fail(function() {
					self.trigger('destroy');
					require([ 'qtip' ], function(qtip) {
						var message = $('<p />', {
							text : 'Failed to create dataset.'
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
				}); */
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
									maxSize : 5*1024*1024,				                    
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