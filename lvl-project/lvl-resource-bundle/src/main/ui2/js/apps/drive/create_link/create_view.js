/**
 * RequireJS module that defines the view: drive->create_link.
 */

define([ 'app', 'tpl!apps/drive/create_link/templates/create_link', 'apps/config/marionette/configuration', 'backbone.syphon' ], function(Lvl, CreateLinkTpl,
		Configuration) {
	Lvl.module('DriveApp.CreateLink.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		View.Content = Marionette.ItemView.extend({
			template : CreateLinkTpl,
			templateHelpers : {
				getDataset : function() {
					return this['filename'];
				}
			},
			events : {
				'click button#create-link-btn' : 'createLink'
			},
			createLink : function(e) {
				e.preventDefault();
				var formData = Backbone.Syphon.serialize(this);
				var requestData = {
					'filename' : this.model.get('filename')
				};
				// submit request to LVL server
				var self = this;
				$('#create-link-btn').attr('disabled', 'disabled');
				var jqxhr = $.ajax({
					type : 'POST',
					contentType : 'application/json',
					crossDomain : true,
					url : config.get('service', '') + '/datasets/open_access/files',
					data : JSON.stringify(requestData),
					headers : config.authorizationHeader()
				}).done(function(data, textStatus, request) {
					self.trigger('destroy');
					require([ 'common/growl' ], function(createGrowl) {
						var anchor = $('<a>', {
							href : request.getResponseHeader('Location')
						})[0];
						var filename = anchor.pathname.substring(anchor.pathname.lastIndexOf('/') + 1);
						createGrowl('New link created', filename + ' <a href="/#drive/links"><i class="fa fa-arrow-circle-right fa-fw"></i> links</a>', false);
					});
				}).fail(function() {
					self.trigger('destroy');
					require([ 'qtip' ], function(qtip) {
						var message = $('<p />', {
							text : 'Failed to create link.'
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
	return Lvl.DriveApp.CreateLink.View;
});