/**
 * RequireJS module that defines the view: access->account-validation.
 */

define([ 'marionette', 'tpl!apps/access/account-validation/templates/account-validation', 'flatui-checkbox', 'flatui-radio' ], function(Marionette,
        AccountValidationTpl) {
    return {
        Content : Marionette.ItemView.extend({
            template : AccountValidationTpl,
            onBeforeRender : function() {
                $('body').addClass('lvl-login-body');
            },
            onClose : function() {
                $('body').removeClass('lvl-login-body');
            }
        })
    };
});