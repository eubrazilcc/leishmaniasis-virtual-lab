/**
 * RequireJS module that defines the view: collection->browse.
 */

define([ 'marionette', 'tpl!apps/collection/browse/templates/collection_browse', 'flatui-checkbox', 'flatui-radio' ], function(Marionette, BrowseTpl) {
    return {
        Content : Marionette.ItemView.extend({
            id : 'browse',
            template : BrowseTpl
        })
    };
});