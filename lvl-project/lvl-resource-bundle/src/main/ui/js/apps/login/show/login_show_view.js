/**
 * RequireJS module that defines the view: login->show.
 */

define([ 'app', 'tpl!apps/login/show/templates/login', 'bootstrapvalidator', 'flatui-checkbox', 'flatui-radio' ], function(Lvl, LoginTpl) {
    Lvl.module('LoginApp.Show.View', function(View, Lvl, Backbone, Marionette, $, _) {
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
            onBeforeRender : function() {
                $('body').addClass('lvl-login-body');
            },
            onShow : function() {
                $(':checkbox').checkbox();
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
                    require([ 'common/views' ], function(CommonViews) {
                        var loadingView = new CommonViews.Loading();
                        Lvl.fullpageRegion.show(loadingView);
                    });

                    // TODO
                    setTimeout(function() {
                        Lvl.fullpageRegion.close();
                    }, 2000);
                    // TODO

                });
            },
            onClose : function() {
                $('body').removeClass('lvl-login-body');
            }
        });
    });
    return Lvl.LoginApp.Show.View;
});