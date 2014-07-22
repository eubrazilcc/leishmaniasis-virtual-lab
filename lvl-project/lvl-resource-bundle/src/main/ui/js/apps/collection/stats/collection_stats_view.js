/**
 * RequireJS module that defines the view: collection->stats.
 */

define([ 'marionette', 'tpl!apps/collection/stats/templates/collection_stats', 'flatui-checkbox', 'flatui-radio' ], function(Marionette, StatsTpl) {
    return {
        Content : Marionette.ItemView.extend({
            id : 'stats',
            template : StatsTpl
        })
    };
});