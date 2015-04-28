/**
 * RequireJS module that defines the view: access->account-validation.
 */

define([ 'app', 'tpl!apps/access/account-validation/templates/account-validation', 'apps/config/marionette/configuration', 'bootstrapvalidator',
		'backbone.syphon' ], function(Lvl, AccountValidationTpl, Configuration) {
	Lvl.module('AccessApp.AccountValidation.View', function(View, Lvl, Backbone, Marionette, $, _) {
		var lvlAuth = new Configuration().get('auth', '');
		View.Content = Marionette.ItemView.extend({
			template : AccountValidationTpl,
			templateHelpers : {
				accountEmail : function() {
					return this.email ? decodeURIComponent(this.email) : '';
				},
				validationCode : function() {
					return this.code ? decodeURIComponent(this.code) : '';
				}
			},
			events : {
				'click button#submit_validation_btn' : 'submitValidation'
			},
			submitValidation : function(e) {
				e.preventDefault();
				$('#submit_validation_btn').attr('disabled', 'disabled');
				// show loading view
				require([ 'common/views' ], function(CommonViews) {
					var loadingView = new CommonViews.Loading();
					Lvl.fullpageRegion.show(loadingView);
				});
				var formData = Backbone.Syphon.serialize(this);
				var requestData = {
					'activationCode' : formData.code,
					'user' : {
						'email' : formData.email
					}
				};
				// submit request to LVL server
				var self = this;
				var jqxhr = $.ajax({
					type : 'PUT',
					contentType : 'application/json',
					crossDomain : true,
					url : lvlAuth + '/pending_users/' + encodeURIComponent(formData.email),
					data : JSON.stringify(requestData)
				}).always(function() {
					Lvl.fullpageRegion.reset();
				}).done(function(data, textStatus, request) {
					Lvl.navigate('login', {
						trigger : true,
						replace : true
					});
				}).fail(function() {
					$('#alertBox').removeClass('hidden');
				});
			},
			onBeforeRender : function() {
				$('body').addClass('lvl-login-body');
			},
			onShow : function() {
				$('#validationForm').on('init.form.bv', function(e, data) {
					data.bv.disableSubmitButtons(true);
				}).bootstrapValidator({
					feedbackIcons : {
						valid : 'fa fa-check',
						invalid : 'fa fa-times',
						validating : 'fa fa-spinner fa-spin'
					},
					submitButtons : 'button[type="submit"]',
					fields : {
						'email' : {
							verbose : false,
							validators : {
								notEmpty : {
									message : 'The email is required and cannot be empty'
								},
								emailAddress : {
									message : 'The input is not a valid email address'
								}
							}
						},
						'code' : {
							validators : {
								notEmpty : {
									message : 'The validation code is required and cannot be empty'
								},
								stringLength : {
									min : 6,
									max : 12,
									message : 'The validation code must be more than 6 and less than 12 characters long'
								}
							}
						}
					}
				}).on('success.field.bv', function(e, data) {
					var isValid = data.bv.isValid();
					data.bv.disableSubmitButtons(!isValid);
				});
				if (this.model.get('email') && this.model.get('code')) {
					$('#validationForm').bootstrapValidator('validate');
					$('#submit_validation_btn').focus();
				}
			},
			onDestroy : function() {
				$('body').removeClass('lvl-login-body');
				Lvl.fullpageRegion.reset();
			}
		});
	});
	return Lvl.AccessApp.AccountValidation.View;
});