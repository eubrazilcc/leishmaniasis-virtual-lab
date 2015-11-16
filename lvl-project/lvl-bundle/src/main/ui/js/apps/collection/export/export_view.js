/**
 * RequireJS module that defines the view: collection->export_dataset.
 */

define([ 'app', 'tpl!apps/collection/export/tpls/collection_export_dataset', 'chance', 'backbone.syphon' ], function(Lvl, ExportDatasetTpl, Chance) {
	Lvl.module('CollectionApp.Export.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		View.Content = Marionette.ItemView.extend({
			template : ExportDatasetTpl,
			templateHelpers : {
				defaultFilename : function() {
					return 'sequences-' + new Chance().string({
						length : 8,
						pool : 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
					});
				}
			},
			serializeData: function() {
				return {
					'sequencesCount' : this.collection.length
				};
			},
			initialize : function(options) {
				this.data_source = options.data_source || 'sandflies';				
			},
			events : {
				'click button#export_export-btn' : 'exportDataset'
			},
			exportDataset : function(e) {
				e.preventDefault();
				var formData = Backbone.Syphon.serialize(this);
				var requestData = {
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
				$('#export_export-btn').attr('disabled', 'disabled');
				var jqxhr = $.ajax({
					type : 'POST',
					contentType : 'application/json',
					crossDomain : true,
					url : Lvl.config.get('service.url') + '/datasets/objects/~',
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
					require([ 'common/alert' ], function(alertDialog) {
						alertDialog('Error', 'Failed to create dataset.');
					});
				});
			}
		});
	});
	return Lvl.CollectionApp.Export.View;
});