/**
 * RequireJS module that defines the view: footer->show.
 */

define([ 'marionette', 'tpl!apps/footer/show/templates/footer', 'flatui-checkbox', 'flatui-radio' ], function(Marionette, FooterTpl) {
    return {
        Footer : Marionette.ItemView.extend({
            id : 'default',
            template : FooterTpl
        })
    };
});