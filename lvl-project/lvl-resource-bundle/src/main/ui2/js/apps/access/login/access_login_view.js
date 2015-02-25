/**
 * RequireJS module that defines the view: access->login.
 */

define([ 'app', 'tpl!apps/access/login/templates/login', 'apps/config/marionette/configuration', 'chance', 'bootstrapvalidator' ], function(Lvl, LoginTpl,
		Configuration, Chance) {
	Lvl.module('AccessApp.Login.View', function(View, Lvl, Backbone, Marionette, $, _) {
		var config = new Configuration();
		View.Content = Marionette.ItemView.extend({
			template : LoginTpl,
			templateHelpers : {
				alertMessage : function() {
					var message;
					switch (this.reason) {
					case "refused":
						message = 'Authorization has been refused for the provided credentials.';
						break;
					case "unauthenticated":
						message = 'The section you are trying to access requires authentication.';
						break
					default:
						message = 'The application has encountered an unknown error.';
					}
					return message;
				}
			},
			events : {
				'click a#signup_linkedin_btn' : 'signupLinkedin'
			},
			signupLinkedin : function(e) {
				e.preventDefault();
				var self = this;
				$('#signup_linkedin_btn').attr('disabled', 'disabled');
				var challenge = new Chance().string({
					length : 16,
					pool : 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
				});
				// register state with LVL server
				var target = self.model.get('target') || 'home';
				var jqxhr = $.ajax({
					type : 'POST',
					contentType : 'application/x-www-form-urlencoded; charset=UTF-8',
					crossDomain : true,
					url : config.get('auth') + '/linkedin/state',
					data : {
						'state' : challenge,
						'redirect_uri' : config.redirectUri(),
						'callback' : config.get('endpoint') + '/auth/linkedin/' + target
					}
				}).done(function(data, textStatus, request) {
					window.location.replace(config.linkedInAuthEndpoint(challenge));
				}).fail(function() {
					$('#alertBox').removeClass('hidden');
				});
			},
			onBeforeRender : function() {
				$('body').addClass('lvl-login-body');
			},
			onShow : function() {
				var target = this.model.get('target') || 'home';
				$('#login-remember').focus();
				$('#login-pass').focus();
				$('#login-email').focus();
				if (this.model.get('reason')) {
					$('#alertBox').removeClass('hidden');
				}
				$('#signinForm').on('init.form.bv', function(e, data) {
					data.bv.disableSubmitButtons(true);
				}).bootstrapValidator({
					submitButtons : 'button[type="submit"]',
					fields : {
						'login-email' : {
							validators : {
								notEmpty : {
									message : 'The email is required and cannot be empty'
								},
								emailAddress : {
									message : 'The input is not a valid email address'
								}
							}
						},
						'login-pass' : {
							validators : {
								notEmpty : {
									message : 'The password is required and cannot be empty'
								}
							}
						}
					}
				}).on('success.field.bv', function(e, data) {
					var isValid = data.bv.isValid();
					data.bv.disableSubmitButtons(!isValid);
				}).on('success.form.bv', function(e) {
					e.preventDefault();
					$('#submit-login-button').blur();
					// show loading view
					require([ 'common/views' ], function(CommonViews) {
						var loadingView = new CommonViews.Loading();
						Lvl.fullpageRegion.show(loadingView);
					});
					// contact the LVL authorization service
					var jqxhr = $.ajax({
						type : 'POST',
						url : config.get('auth') + '/token',
						data : {
							'client_id' : config.get('oauth2_app').client_id,
							'client_secret' : config.get('oauth2_app').client_secret,
							'grant_type' : 'password',
							'username' : $('input[name=login-email]').val(),
							'password' : $('input[name=login-pass]').val(),
							'use_email' : 'true'
						},
						headers : {
							'Content-Type' : 'application/x-www-form-urlencoded'
						}
					}).always(function() {
						Lvl.fullpageRegion.destroy();
					});
					jqxhr.done(function(data) {
						if (data['access_token'] !== undefined) {
							config.saveSession($('input[name=login-email]').val(), data['access_token'], $('input[name=login-remember]').is(":checked"));
							Lvl.navigate(decodeURIComponent(target), {
								trigger : true
							});
						} else {
							Lvl.navigate('login/' + target + '/invalid_server_response', {
								trigger : true,
								replace : true
							});
						}
					});
					jqxhr.fail(function() {
						Lvl.navigate('login/' + target + '/refused', {
							trigger : true,
							replace : true
						});
					});
				});
			},
			onDestroy : function() {
				$('body').removeClass('lvl-login-body');
			}
		});
	});
	return Lvl.AccessApp.Login.View;
});