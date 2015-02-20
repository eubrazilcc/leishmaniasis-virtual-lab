/**
 * RequireJS module that defines the view: collection->export_dataset.
 */

define([ 'app', 'tpl!apps/collection/export/templates/collection_export_dataset', 'apps/config/marionette/configuration', 'chance', 'backbone.syphon' ],
		function(Lvl, ExportDatasetTpl, Configuration, Chance) {
			Lvl.module('CollectionApp.Export.View', function(View, Lvl, Backbone, Marionette, $, _) {
				'use strict';
				var config = new Configuration();
				View.Content = Marionette.ItemView.extend({
					template : ExportDatasetTpl,
					events : {
						'click button#export-btn' : 'exportDataset'
					},
					templateHelpers : {
						defaultFilename : function() {
							return 'sequences-' + new Chance().string({
								length : 8,
								pool : 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
							});
						}
					},
					exportDataset : function(e) {
						e.preventDefault();
						var formData = Backbone.Syphon.serialize(this);
						var requestData = {
							'filename' : formData.filename_input,
							'metadata' : {
								'description' : formData.description_input,
								'target' : {
									'collection' : 'sandflies',
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
							url : config.get('service', '') + '/datasets/objects/files',
							data : JSON.stringify(requestData),
							headers : config.authorizationHeader()
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
						});
					}
				});
			});
			return Lvl.CollectionApp.Export.View;
		});