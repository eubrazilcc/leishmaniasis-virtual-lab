/**
 * RequireJS module that defines the view: e-compendium->submit.
 */

define([ 'marionette', 'tpl!apps/e-compendium/submit/templates/e-compendium_submit', 'flatui-checkbox', 'flatui-radio' ], function(Marionette, SubmitTpl) {
    return {
        Content : Marionette.ItemView.extend({
            id : 'submit',
            template : SubmitTpl
        })
    };
});