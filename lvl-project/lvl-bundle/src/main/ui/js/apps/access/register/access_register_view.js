/**
 * RequireJS module that defines the view: login->register.
 */

define([ 'app', 'tpl!apps/access/register/templates/register', 'apps/config/marionette/styles/style', 'apps/config/marionette/configuration',
        'bootstrapvalidator', 'flatui-checkbox', 'flatui-radio' ], function(Lvl, RegisterTpl, Style, Configuration) {
    Lvl.module('AccessApp.Register.View', function(View, Lvl, Backbone, Marionette, $, _) {
        var lvlAuth = new Configuration().get('auth', '');
        View.Content = Marionette.ItemView.extend({
            template : RegisterTpl,
            onBeforeRender : function() {
                require([ 'entities/styles' ], function() {
                    new Style().loadCss(Lvl.request('styles:form-validation:entities').toJSON());
                });
                $('body').addClass('lvl-login-body');
            },
            onShow : function() {
                $('#signupForm').on('init.form.bv', function(e, data) {
                    data.bv.disableSubmitButtons(true);
                }).bootstrapValidator({
                    feedbackIcons : {
                        valid : 'fa fa-check',
                        invalid : 'fa fa-times',
                        validating : 'fa fa-refresh'
                    },
                    submitButtons : 'button[type="submit"]',
                    fields : {
                        'username' : {
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
                                    }
                                }
                            }
                        },
                        'email' : {
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
                                    }
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
            onClose : function() {
                $('body').removeClass('lvl-login-body');
            }
        });
    });
    return Lvl.AccessApp.Register.View;
});