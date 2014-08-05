/**
 * RequireJS module that defines the view: login->show.
 */

define([ 'app', 'tpl!apps/login/show/templates/login', 'flatui-checkbox', 'flatui-radio' ], function(Lvl, LoginTpl) {
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
                $('#login-email').focus();
                if (this.model.get('reason')) {
                    $('#alertBox').removeClass('hidden');
                }
            },
            onClose : function() {
                $('body').removeClass('lvl-login-body');
            }
        });
    });
    return Lvl.LoginApp.Show.View;
});