/**
 * RequireJS module that defines the view: collection->submit.
 */

define([ 'app', 'tpl!apps/collection/submit/tpls/collection_submit', 'chance', 'pace', 'bootstrapvalidator', 'backbone.syphon' ], function(Lvl, SubmitTpl, Chance, pace) {
	Lvl.module('CollectionApp.Submit.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var lvlService = Lvl.config.get('service.url');
		View.Content = Marionette.ItemView.extend({
			id : 'submit',
			template : SubmitTpl,
			templateHelpers : function() {
				return {					
					pendingId : function() {
						return 'user-' + new Chance().string({
							length : 8,
							pool : 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
						});
					}					
				}
			},
			events : {
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
					                        regexp: /^[ACTG]+$/i,
					                        message: 'The origin can consist of A, C, T and G only'
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
				}, 'click #lvlSubmitSequenceBtn' : function(e) {
					e.preventDefault();
					pace.restart();
					$('#lvlSubmitSequenceForm button[type="submit"]').attr('disabled', 'disabled');
					var formData = Backbone.Syphon.serialize(this);
					var request = new FormData();
					var issueBlob = new Blob([ JSON.stringify({
						'sample' : {							
						    'institutionCode' : formData.inputInstitution,
						    'collectionCode' : formData.inputInstitution + '-collection',
						    'continent' : 'Europe', // TODO
						    'country' : 'Spain', // TODO
						    'stateProvince' : 'Madrid', // TODO
						    'county' : 'Madrid', // TODO
						    'locality' : 'Fuenlabrada', // TODO
						    'decimalLatitude' : formData.inputLatitude,
						    'decimalLongitude' : formData.inputLongitude,
						    'scientificName' : 'Leishmania infantum',
						    'phylum' : 'Euglenozoa', // TODO
						    'clazz' : 'Kinetoplastea', // TODO
						    'order' : 'Trypanosomatida', // TODO
						    'family' : 'Trypanosomatidae', // TODO
						    'genus' : 'Leishmania', // TODO
						    'specificEpithet' : 'infantum' // TODO
						},
						'sequence' : formData.inputOrigin
					}) ], {
						type : 'application/json'
					});
					request.append('issue', issueBlob);
					
					/*
				|| isBlank(trimToNull(pendingSeq.getSample().getCountry()))
				|| isBlank(trimToNull(pendingSeq.getSample().getScientificName())))
					*/
					
					/* TODO 
					
					var inputScreenshot = $('#screenshot');
					if (inputScreenshot.val()) {
						request.append('file', inputScreenshot.get(0).files[0]);
					}
					// submit request to LVL server
					var self = this;
					var jqxhr = $.ajax({
						type : 'POST',
						processData : false,
						contentType : false,
						crossDomain : true,
						url : lvlService + '/support/issues/with-attachment',
						data : request
					}).always(function() {
						pace.stop();
						var form = $('#lvlIssueReportForm');
						form.bootstrapValidator('resetForm', true);
						form.bootstrapValidator('disableSubmitButtons', true);
					}).done(
							function(data, textStatus, request) {
								require([ 'common/growl' ], function(createGrowl) {
									createGrowl('New issue report created', 'Our team will investigate your report and will send you a '
											+ 'follow-up e-mail at the address that you provided.', false);
								});
							}).fail(function(jqXHR, textStatus, errorThrown) {
						if (jqXHR.status !== 404) {
							require([ 'common/alert' ], function(alertDialog) {
								alertDialog('Error', 'The issue report cannot be sent.');
							});
						}
					}); */
					
					// TODO
					console.log('FORM SUBMITTED!', JSON.stringify(issueBlob));
					// TODO
				}
			}			
		});
	});
	return Lvl.CollectionApp.Submit.View;
});