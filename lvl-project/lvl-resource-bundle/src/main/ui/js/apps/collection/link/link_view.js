/**
 * RequireJS module that defines the view: collection->create_link.
 */

define([ 'app', 'tpl!apps/collection/link/templates/collection_create_link', 'apps/config/marionette/configuration', 'backbone.syphon' ], function(Lvl,
		CreateLinkTpl, Configuration) {
	Lvl.module('CollectionApp.Link.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		View.Content = Marionette.ItemView.extend({
			template : CreateLinkTpl,
			events : {
				'click button#create-btn' : 'createLink'
			},
			createLink : function(e) {
				e.preventDefault();
				var formData = Backbone.Syphon.serialize(this);
				var requestData = {
					'description' : formData.description_input,
					'downloadUri' : null,
					'links' : null,
					'mime' : null,
					'owner' : null,
					'path' : null,
					'target' : {
						'compression' : formData.compression_select,
						'filter' : formData.filter_select,
						'ids' : _.pluck(this.collection.toJSON(), 'id'),
						'type' : 'sequence',
						'collection' : 'sandflies'
					}
				};
				// submit request to LVL server
				var self = this;
				$('#create-btn').attr('disabled', 'disabled');
				var jqxhr = $.ajax({
					type : 'POST',
					contentType : 'application/json',
					crossDomain : true,
					url : config.get('service', '') + '/public_links',
					data : JSON.stringify(requestData),
					headers : config.authorizationHeader()
				}).done(function(data, textStatus, request) {
					self.trigger('close');
					require([ 'common/growl' ], function(createGrowl) {
						var anchor = $('<a>', {
							href : request.getResponseHeader('Location')
						})[0];
						var filename = anchor.pathname.substring(anchor.pathname.lastIndexOf('/') + 1);
						createGrowl('New link created', filename + ' <a href="/#files/links"><i class="fa fa-hand-o-right fa-fw"></i> links</a>', false);
					});
				}).fail(function() {
					self.trigger('close');
					require([ 'qtip' ], function(qtip) {
						var message = $('<p />', {
							text : 'Failed to create public link in the LVL service.'
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
	return Lvl.CollectionApp.Link.View;
});