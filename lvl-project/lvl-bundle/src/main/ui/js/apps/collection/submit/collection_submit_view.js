/**
 * RequireJS module that defines the view: collection->submit.
 */

define([ 'marionette', 'tpl!apps/collection/submit/templates/collection_submit', 'flatui-checkbox', 'flatui-radio' ], function(Marionette, SubmitTpl) {
    return {
        Content : Marionette.ItemView.extend({
            id : 'submit',
            template : SubmitTpl
        })
    };
});