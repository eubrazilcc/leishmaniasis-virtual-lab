/**
 * RequireJS module that defines the view: curation->submission_resolver.
 */

define([ 'app', 'tpl!apps/curation/submission_resolver/tpls/curation_submission_resolver', 'pace', 'backbone.oauth2', 'bootstrapvalidator', 'backbone.syphon' ],
		function(Lvl, ResolverTpl, pace) {
	Lvl.module('CurationApp.SubmissionResolver.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		View.Content = Marionette.ItemView.extend({
			template : ResolverTpl,			
			initialize : function(options) {
				this.section = options.section;
				this.collectionId = options.collectionId;
				this.item = options.item;
			},
			events : {
				'focus #lvlSubmissionResolutionForm select.form-control' : function(e) {
					var form = $('#lvlSubmissionResolutionForm');
					if (Boolean(form.attr('data-pristine') === 'true')) {
						form.attr('data-pristine', 'false');
						form.on('init.form.bv', function(e, data) {
							data.bv.disableSubmitButtons(true);
						}).bootstrapValidator({
							submitButtons : 'button[type="submit"]',
							fields : {
								'selectResolution' : {
									verbose : false,
									validators : {
										notEmpty : {
											message : 'The resolution is required and cannot be empty'
										}
									}
								}														
							}
						}).on('success.field.bv', function(e, data) {
							var isValid = data.bv.isValid();
							data.bv.disableSubmitButtons(!isValid);
						});
					}
				}, 'click #lvlSubmissionResolutionBtn' : function(e) {
					e.preventDefault();
					pace.restart();
					$('#lvlSubmissionResolutionForm button[type="submit"]').attr('disabled', 'disabled');
					$('#lvlSubmissionResolutionBtn').tooltip('hide');
					var self = this, formData = Backbone.Syphon.serialize(this);
					self.item.set({
						status : 'CLOSED',
						resolution : formData.selectResolution,
						allocatedCollection : formData.inputCollection,
						allocatedId : formData.inputItemId
					});					
					var jqxhr = $.ajax({
						url : Lvl.config.get('service.url') + '/pending/' + (self.collectionId ? self.collectionId : self.section) + '/~/' + self.item.get('id'),
						type: 'PUT',
						crossDomain : true,
						contentType : 'application/json',
						headers : Lvl.config.authorizationHeader(),							
						data : JSON.stringify(self.item)
					}).always(function() {
						pace.stop();
						var form = self.$('#lvlSubmissionResolutionForm');
						form[0].reset();
						form.bootstrapValidator('resetForm', true);
						form.bootstrapValidator('disableSubmitButtons', true);
					}).done(function(data, textStatus, request) {
						self.trigger('destroy');
						if (200 === request.status || 204 === request.status) {
							// TODO self.collection.set([ item ]);
						} else {
							require([ 'common/alert' ], function(alertDialog) {
								alertDialog('Error', 'The server response was not OK.');
							});
						}
					}).fail(function(jqXHR, textStatus, errorThrown) {
						self.trigger('destroy');
						require([ 'common/alert' ], function(alertDialog) {
							alertDialog('Error', 'The submission cannot be resolved.');
						});
					});
				}
			},
			onDestroy : function() {
				pace.stop();
				this.stopListening();
			},
			onRender : function() {
				pace.start();
			}
		});
	});
	return Lvl.CurationApp.SubmissionResolver.View;
});