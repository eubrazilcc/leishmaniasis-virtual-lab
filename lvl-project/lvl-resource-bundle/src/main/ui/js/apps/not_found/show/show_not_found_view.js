/**
 * RequireJS module that defines the view: not-found->show.
 */

define([ 'marionette', 'tpl!apps/not_found/show/templates/not_found', 'flatui-checkbox', 'flatui-radio' ], function(Marionette, NotFoundTpl) {

    return {
        Content : Marionette.ItemView.extend({
            template : NotFoundTpl
        })
    };

});