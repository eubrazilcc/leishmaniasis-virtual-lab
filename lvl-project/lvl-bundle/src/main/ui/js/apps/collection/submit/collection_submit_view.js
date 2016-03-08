/**
 * RequireJS module that defines the view: collection->submit.
 */

define([ 'app', 'tpl!apps/collection/submit/tpls/collection_submit', 'chance', 'pace', 'backbone.oauth2', 'bootstrapvalidator', 'backbone.syphon' ], function(Lvl, SubmitTpl, Chance, pace) {
	Lvl.module('CollectionApp.Submit.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var lvlService = Lvl.config.get('service.url');
		View.Content = Marionette.ItemView.extend({
			id : 'submit',
			template : SubmitTpl,
			templateHelpers : function() {
				return {					
					submissionId : function() {
						return 'user-seq-' + new Chance().string({
							length : 8,
							pool : 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
						});
					}
				}
			},
			events : {
				'change [name="inputLongitude"]' : 'geocodeFn',
				'change [name="inputLatitude"]' : 'geocodeFn',
				'change [name="inputOrganism"]' : function(e) {
					var self = this;
					var organism = $(e.target).val();
					var form = $('#lvlSubmitSequenceForm');
					if (organism === 'Leishmania') {
						self.$('#inputPhylum').val('Euglenozoa');
						self.$('#inputClass').val('Kinetoplastea');
						self.$('#inputOrder').val('Trypanosomatida');
						self.$('#inputFamily').val('Trypanosomatidae');
						self.$('#inputGenus').val('Leishmania');
						form.bootstrapValidator('resetField', 'inputEpithet', true);
						self.$('#inputScientificName').val('');
						form.bootstrapValidator('resetField', 'inputIndividualCount', true);
						self.$('#inputIndividualCount').val('1');
					} else if (organism === 'Sandflies') {
						self.$('#inputPhylum').val('Arthropoda');
						self.$('#inputClass').val('Insecta');
						self.$('#inputOrder').val('Diptera');
						self.$('#inputFamily').val('Psychodidae');
						form.bootstrapValidator('resetField', 'inputGenus', true);
						form.bootstrapValidator('resetField', 'inputEpithet', true);
						self.$('#inputScientificName').val('');
						form.bootstrapValidator('resetField', 'inputIndividualCount', true);
						self.$('#inputIndividualCount').val('');
					}
					form.bootstrapValidator('validateField', 'inputGenus');
					form.bootstrapValidator('validateField', 'inputIndividualCount');
				},
				'change #inputGenus' : function(e) {
					var self = this;
					self.$('#inputScientificName').val(self.$('#inputGenus').val() + ' ' + self.$('#inputEpithet').val());
				},
				'change #inputEpithet' : function(e) {
					var self = this;
					self.$('#inputScientificName').val(self.$('#inputGenus').val() + ' ' + self.$('#inputEpithet').val());
				},
				'focus #lvlSubmitSequenceForm input.form-control' : function(e) {
					var form = $('#lvlSubmitSequenceForm');
					if (Boolean(form.attr('data-pristine') === 'true')) {
						form.attr('data-pristine', 'false');
						form.on('init.form.bv', function(e, data) {
							data.bv.disableSubmitButtons(true);
						}).bootstrapValidator({
							submitButtons : 'button[type="submit"]',
							fields : {
								'inputDefinition' : {
									verbose : false,
									validators : {
										notEmpty : {
											message : 'The definition is required and cannot be empty'
										},
										stringLength : {
											min : 3,
											max : 256,
											message : 'The definition is required to be of 3-256 characters in length'
										}
									}
								},
								'inputGenus' : {
									verbose : false,
									validators : {
										notEmpty : {
											message : 'The genus is required and cannot be empty'
										},
										stringLength : {
											min : 3,
											max : 128,
											message : 'The genus is required to be of 3-128 characters in length'
										}
									}
								},
								'inputEpithet' : {
									verbose : false,
									validators : {
										notEmpty : {
											message : 'The epithet is required and cannot be empty'
										},
										stringLength : {
											min : 3,
											max : 128,
											message : 'The epithet is required to be of 3-128 characters in length'
										}
									}
								},
								'inputLongitude' : {
									verbose : false,
									validators : {
										notEmpty : {
											message : 'The longitude is required and cannot be empty'
										},
										numeric : {
											message: 'The value is not a number'
										},
										between : {
					                        min : -180,
					                        max : 180,
					                        message : 'The longitude must be between -180.0 and 180.0'
					                    }
									}
								},
								'inputLatitude' : {
									verbose : false,
									validators : {
										notEmpty : {
											message : 'The latitude is required and cannot be empty'
										},
										numeric : {
											message : 'The value is not a number'
										},
										between : {
					                        min : -90,
					                        max : 90,
					                        message : 'The latitude must be between -90.0 and 90.0'
					                    }
									}
								},
								'inputGene' : {
									verbose : false,
									validators : {
										notEmpty : {
											message : 'The gene is required and cannot be empty'
										},
										stringLength : {
											min : 3,
											message : 'The gene is required to be a minimum of 3 characters in length'
										}
									}
								},
								'inputOrigin' : {
									verbose : false,
									validators : {
										notEmpty : {
											message : 'The origin is required and cannot be empty'
										},
										stringLength : {
											min : 60,
											message : 'The origin is required to be a minimum of 60 characters in length'
										},
										regexp: {
					                        regexp: /^[ACTGN\-]+$/i,
					                        message: 'The origin can consist of A, C, T, G, N and - only'
					                    }
									}
								},
								'inputIndividualCount' : {
									verbose : false,
									validators : {										
										integer : {
											message : 'The value is not an integer'
										},
										between : {
					                        min : 0,
					                        max : 1000,
					                        message : 'The individual count must be between 0 and 1000'
					                    }
									}
								},
								'inputInstitution' : {
									verbose : false,
									validators : {
										notEmpty : {
											message : 'The institution is required and cannot be empty'
										},
										stringLength : {
											min : 3,
											max : 256,
											message : 'The institution is required to be of 3-256 characters in length'
										}
									}
								}								
							}
						}).on('success.field.bv', function(e, data) {
							var isValid = data.bv.isValid();
							data.bv.disableSubmitButtons(!isValid);
						});
					}
				}, 'click #lvlResetSequenceBtn' : function(e) {
					e.preventDefault();
					var form = $('#lvlSubmitSequenceForm');
					form[0].reset();
					form.bootstrapValidator('resetForm', true);
					form.bootstrapValidator('disableSubmitButtons', true);
				}, 'click #lvlSubmitSequenceBtn' : function(e) {
					e.preventDefault();
					pace.restart();
					$('#lvlSubmitSequenceForm button[type="submit"]').attr('disabled', 'disabled');
					$('#lvlSubmitSequenceBtn').tooltip('hide');
					var self = this, formData = Backbone.Syphon.serialize(this), organism = formData.inputOrganism.toLowerCase();
					require([ 'entities/pending_sequence' ], function(PendingSequenceModel) {
						var pendingSeq = new PendingSequenceModel.PendingSequenceCreate();
						pendingSeq.oauth2_token = Lvl.config.authorizationToken();
						pendingSeq.data_source = organism;
						pendingSeq.save({
							'sample' : {							
							    'institutionCode' : formData.inputInstitution,
							    'collectionCode' : formData.inputInstitution + '-collection',
							    'country' : formData.inputCountry,
							    'stateProvince' : formData.inputStateProvince,
							    'county' : formData.inputCounty,
							    'locality' : formData.inputLocality,
							    'decimalLatitude' : formData.inputLatitude,
							    'decimalLongitude' : formData.inputLongitude,
							    'scientificName' : formData.inputScientificName,
							    'phylum' : formData.inputPhylum,
							    'clazz' : formData.inputClass,
							    'order' : formData.inputOrder,
							    'family' : formData.inputFamily,
							    'genus' : formData.inputGenus,
							    'specificEpithet' : formData.inputEpithet
							},
							'sequence' : formData.inputOrigin,
							'preparation' : {
								'sex' : formData.inputSex,
								'individualCount' : formData.inputIndividualCount,
								'collectingMethod' : formData.inputCollectingMethod,
								'preparationType' : formData.inputPreparationType,
								'materialType' : formData.inputMaterialType
							}
						}, {							
							success : function(model, resp, options) {
								require([ 'common/growl' ], function(createGrowl) {
									var anchor = $('<a>', {
										href : options.xhr.getResponseHeader('Location')
									})[0];
									var id = anchor.pathname.substring(anchor.pathname.lastIndexOf('/') + 1);
									createGrowl('New sequence created', id
										+ ' <a href="/#collection/pending/' + organism + '"><i class="fa fa-arrow-circle-right fa-fw"></i> pending sequences</a>', false);
								});
							},
							error : function(model, resp, options) {
								require([ 'common/alert' ], function(alertDialog) {
									alertDialog('Error', 'Failed to create sequence.');
								});
							}
						}).always(function() {
							pace.stop();
							var form = self.$('#lvlSubmitSequenceForm');
							form[0].reset();
							form.bootstrapValidator('resetForm', true);
							form.bootstrapValidator('disableSubmitButtons', true);
						});
					});					
				}
			},
			geocodeFn : function(e) {				
				var self = this;
				var form = $('#lvlSubmitSequenceForm'), bootstrapValidator = form.data('bootstrapValidator');
				if (bootstrapValidator.isValidField('inputLongitude') && bootstrapValidator.isValidField('inputLatitude')) {
					var lng = this.$('#inputLongitude').val(), lat = this.$('#inputLatitude').val();
					pace.restart();
					var jqxhr = $.ajax({
						crossDomain : true,
						url : Lvl.config.googleGeocodeEndpoint(lat, lng)						
					}).always(function() {
						pace.stop();						
					}).done(function(data, textStatus, request) {
						if ('OK' === data.status) {
							require([ 'common/geocode_parser' ], function(parseGeocode) {
								var address = parseGeocode(data);							
								self.$('#inputCountry').val(address.country);
								self.$('#inputStateProvince').val(address.stateProvince);
								self.$('#inputCounty').val(address.county);
								self.$('#inputLocality').val(address.locality);
							});
						} else {
							require([ 'common/alert' ], function(alertDialog) {
								alertDialog('Error', 'A location cannot be identified from the provided coordinates.');
							});
							self.resetAddress();
						}
					}).fail(function(jqXHR, textStatus, errorThrown) {
						require([ 'common/alert' ], function(alertDialog) {
							alertDialog('Error', 'A location cannot be identified from the provided coordinates.');
						});
					});
				} else self.resetAddress();
			},
			resetAddress : function() {
				var self = this;
				self.$('#inputCountry').val('Unknown');
				self.$('#inputStateProvince').val('');
				self.$('#inputCounty').val('');
				self.$('#inputLocality').val('');
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
	return Lvl.CollectionApp.Submit.View;
});