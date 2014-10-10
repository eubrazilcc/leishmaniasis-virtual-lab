/**
 * RequireJS module that defines the view: collection->create_link.
 */

define([ 'app', 'tpl!apps/collection/link/templates/collection_create_link', 'apps/config/marionette/configuration', 'backbone.syphon' ], function(Lvl,
		CreateLinkTpl, Configuration) {
	Lvl.module('CollectionApp.Link.View', function(View, Lvl, Backbone, Marionette, $, _) {
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
						'type' : 'sequence'
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
				}).done(function() {
					self.trigger('close');
				}).fail(function() {
					self.trigger('close');
					require([ 'qtip' ], function(qtip) {
						var message = $('<p />', {
							text : 'The public link cannot be created.'
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