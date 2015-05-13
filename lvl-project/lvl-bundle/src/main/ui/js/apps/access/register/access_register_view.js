/**
 * RequireJS module that defines the view: login->register.
 */

define([ 'app', 'tpl!apps/access/register/tpls/register', 'bootstrapvalidator', 'backbone.syphon' ], function(Lvl, RegisterTpl) {
	Lvl.module('AccessApp.Register.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var lvlAuth = Lvl.config.get('auth', '');
		View.Content = Marionette.ItemView.extend({
			template : RegisterTpl,
			events : {
				'click a#privacy_policy_btn' : 'showPrivacyPolicy',
				'click a#terms_and_conditions_btn' : 'showTermsAndConditions',
				'click button#sign_up_btn' : 'signUp'
			},
			showPrivacyPolicy : function(e) {
				e.preventDefault();
				this.trigger('access:view:privacy_policy');
			},
			showTermsAndConditions : function(e) {
				e.preventDefault();
				this.trigger('access:view:terms_and_conditions');
			},
			signUp : function(e) {
				e.preventDefault();
				$('#sign_up_btn').attr('disabled', 'disabled');
				// show loading view
				require([ 'common/views' ], function(CommonViews) {
					var loadingView = new CommonViews.Loading();
					Lvl.fullpageRegion.show(loadingView);
				});
				var formData = Backbone.Syphon.serialize(this);
				var requestData = {
					'userid' : formData.username,
					'email' : formData.email,
					'password' : formData.password,
					'firstname' : formData.firstname,
					'lastname' : formData.lastname,
					'industry' : formData.institution_type,
					'positions' : [ formData.institution ]
				};
				// submit request to LVL server
				var self = this;
				var jqxhr = $.ajax({
					type : 'POST',
					contentType : 'application/json',
					crossDomain : true,
					url : lvlAuth + '/pending_users',
					data : JSON.stringify(requestData)
				}).always(function() {
					Lvl.fullpageRegion.reset();
				}).done(function(data, textStatus, request) {
					Lvl.navigate('account/validation/' + encodeURIComponent(formData.email), {
						trigger : true,
						replace : true
					});
				}).fail(function() {
					$('#alertBox').removeClass('hidden');
				});
			},			
			onShow : function() {
				$('#signupForm').on('init.form.bv', function(e, data) {
					data.bv.disableSubmitButtons(true);
				}).bootstrapValidator({
					feedbackIcons : {
						valid : 'fa fa-check',
						invalid : 'fa fa-times',
						validating : 'fa fa-spinner fa-spin'
					},
					submitButtons : 'button[type="submit"]',
					fields : {
						'username' : {
							verbose : false,
							validators : {
								notEmpty : {
									message : 'The username is required and cannot be empty'
								},
								different : {
									field : 'password',
									message : 'The username cannot be same as password'
								},
								stringLength : {
									min : 3,
									max : 24,
									message : 'The username must be more than 3 and less than 24 characters long'
								},
								regexp : {
									regexp : /^[a-zA-Z0-9_]+$/,
									message : 'The username can only consist of alphabetical, number and underscore'
								},
								remote : {
									message : 'The username is not available',
									url : lvlAuth + '/pending_users/check_availability',
									data : {
										type : 'username'
									},
									type : 'POST',
									delay : 2000,
									threshold : 3
								}
							}
						},
						'firstname' : {
							verbose : false,
							validators : {
								notEmpty : {
									message : 'The name is required and cannot be empty'
								},
								stringLength : {
									min : 1,
									max : 128,
									message : 'The name must be more than 1 and less than 128 characters long'
								}
							}
						},
						'lastname' : {
							verbose : false,
							validators : {
								notEmpty : {
									message : 'The lastname is required and cannot be empty'
								},
								stringLength : {
									min : 1,
									max : 128,
									message : 'The lastname must be more than 1 and less than 128 characters long'
								}
							}
						},
						'email' : {
							verbose : false,
							validators : {
								notEmpty : {
									message : 'The email is required and cannot be empty'
								},
								emailAddress : {
									message : 'The input is not a valid email address'
								},
								remote : {
									message : 'The email is not available',
									url : lvlAuth + '/pending_users/check_availability',
									data : {
										type : 'email'
									},
									type : 'POST',
									delay : 2000,
									threshold : 3
								}
							}
						},
						'password' : {
							validators : {
								notEmpty : {
									message : 'The password is required and cannot be empty'
								},
								different : {
									field : 'username',
									message : 'The password cannot be same as username'
								},
								stringLength : {
									min : 6,
									max : 32,
									message : 'The password must be more than 6 and less than 32 characters long'
								}
							}
						}
					}
				}).on('success.field.bv', function(e, data) {
					var isValid = data.bv.isValid();
					data.bv.disableSubmitButtons(!isValid);
				});
			},
			onDestroy : function() {
				$('body').removeClass('lvl-login-body');
				Lvl.fullpageRegion.reset();
			}
		});
	});
	return Lvl.AccessApp.Register.View;
});