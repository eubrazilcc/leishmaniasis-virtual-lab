/**
 * RequireJS module that defines the view: collection->browse.
 */

define([ 'marionette', 'tpl!apps/collection/browse/templates/browse', 'flatui-checkbox', 'flatui-radio' ], function(Marionette, BrowseTpl) {

    return {
        Content : Marionette.ItemView.extend({
            template : BrowseTpl
        })
    };

});