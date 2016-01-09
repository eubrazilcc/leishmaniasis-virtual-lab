/**
 * RequireJS module that defines the view: e-compendium->submit.
 */

define([ 'app', 'tpl!apps/e-compendium/submit/tpls/e-compendium_submit', 'chance', 'pace', 'backbone.oauth2', 'bootstrapvalidator', 'backbone.syphon' ], function(Lvl, SubmitTpl, Chance, pace) {
	Lvl.module('ECompendiumApp.Submit.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var lvlService = Lvl.config.get('service.url');
		View.Content = Marionette.ItemView.extend({
			id : 'submit',
			template : SubmitTpl,
			templateHelpers : function() {
				return {					
					submissionId : function() {
						return 'user-cit-' + new Chance().string({
							length : 8,
							pool : 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
						});
					}
				}
			},
			events : {
				'change [name="inputPMID"]' : function() {
					var self = this;
					var form = $('#lvlSubmitCitationForm'), bootstrapValidator = form.data('bootstrapValidator');
					if (bootstrapValidator.isValidField('inputPMID')) {
						var pmid = this.$('#inputPMID').val();
						pace.restart();
						var jqxhr = $.ajax({
							type: 'POST',
							crossDomain : true,
							url : Lvl.config.ncbiEfetchEndpoint(),
							data : 'db=pubmed&rettype=abstract&retmode=text&id=' + pmid
						}).always(function() {
							pace.stop();
						}).done(function(data, textStatus, request) {							
							if (200 === request.status) {
								self.$('#inputAbstract').val(data);								
							} else {
								require([ 'common/alert' ], function(alertDialog) {
									alertDialog('Error', 'Cannot get the abstract associated to the provided PMID.');
								});
								self.$('#inputAbstract').val('');
							}
						}).fail(function(jqXHR, textStatus, errorThrown) {
							require([ 'common/alert' ], function(alertDialog) {
								alertDialog('Error', 'Cannot get the abstract associated to the provided PMID.');
							});
						});
					} else self.$('#inputAbstract').val('');					
				},
				'focus #lvlSubmitCitationForm input.form-control' : function(e) {
					var form = $('#lvlSubmitCitationForm');
					if (Boolean(form.attr('data-pristine') === 'true')) {
						form.attr('data-pristine', 'false');
						form.on('init.form.bv', function(e, data) {
							data.bv.disableSubmitButtons(true);
						}).bootstrapValidator({
							submitButtons : 'button[type="submit"]',
							fields : {
								'inputPMID' : {
									verbose : false,
									validators : {
										notEmpty : {
											message : 'The PMID is required and cannot be empty'
										},
										numeric : {
											message: 'The value is not a number'
										}
									}
								}
							}
						}).on('success.field.bv', function(e, data) {
							var isValid = data.bv.isValid();
							data.bv.disableSubmitButtons(!isValid);
						});
					}
				}, 'click #lvlResetCitationBtn' : function(e) {
					e.preventDefault();
					var form = $('#lvlSubmitCitationForm');
					form[0].reset();
					form.bootstrapValidator('resetForm', true);
					form.bootstrapValidator('disableSubmitButtons', true);
				}, 'click #lvlSubmitCitationBtn' : function(e) {
					e.preventDefault();
					pace.restart();
					$('#lvlSubmitCitationForm button[type="submit"]').attr('disabled', 'disabled');
					$('#lvlSubmitCitationBtn').tooltip('hide');
					var self = this, formData = Backbone.Syphon.serialize(this);
					require([ 'entities/pending_citation' ], function(PendingCitationModel) {
						var pendingCit = new PendingCitationModel.PendingCitationCreate();
						pendingCit.oauth2_token = Lvl.config.authorizationToken();
						pendingCit.save({
							'pubmedId' : formData.inputPMID
						}, {
							success : function(model, resp, options) {
								require([ 'common/growl' ], function(createGrowl) {
									var anchor = $('<a>', {
										href : options.xhr.getResponseHeader('Location')
									})[0];
									var id = anchor.pathname.substring(anchor.pathname.lastIndexOf('/') + 1);
									createGrowl('New citation created', id
										+ ' <a href="/#e-compendium/pending"><i class="fa fa-arrow-circle-right fa-fw"></i> pending citations</a>', false);
								});
							},
							error : function(model, resp, options) {
								require([ 'common/alert' ], function(alertDialog) {
									alertDialog('Error', 'Failed to create citation.');
								});
							}
						}).always(function() {
							pace.stop();
							var form = self.$('#lvlSubmitCitationForm');
							form[0].reset();
							form.bootstrapValidator('resetForm', true);
							form.bootstrapValidator('disableSubmitButtons', true);
						});
					});					
				}
			},			
			onRender : function() {
				this.$el.find('[data-toggle="tooltip"]').tooltip();
			},
			onDestroy : function() {
				pace.stop();
				this.stopListening();
			}
		});
	});
	return Lvl.ECompendiumApp.Submit.View;
});