/**
 * RequireJS module that defines the view: login->show.
 */

define([ 'marionette', 'tpl!apps/login/show/templates/login', 'flatui-checkbox', 'flatui-radio' ], function(Marionette, LoginTpl) {

    return {
        Content : Marionette.ItemView.extend({
            template : LoginTpl,
            onShow : function() {
                $('body').addClass('lvl-login-body');
                $(':checkbox').checkbox();
                $('#login-email').focus();
            },
            onClose : function() {
                $('body').removeClass('lvl-login-body');
            }
        })
    };

});