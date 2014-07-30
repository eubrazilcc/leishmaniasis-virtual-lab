/**
 * RequireJS module that defines the view: login->account-validation.
 */

define([ 'marionette', 'tpl!apps/login/account-validation/templates/account-validation', 'flatui-checkbox', 'flatui-radio' ], function(Marionette,
        AccountValidationTpl) {
    return {
        Content : Marionette.ItemView.extend({
            template : AccountValidationTpl,
            onBeforeRender : function() {
                // TODO $('body').addClass('lvl-login-body');
            },
            onClose : function() {
                // TODO $('body').removeClass('lvl-login-body');
            }
        })
    };
});