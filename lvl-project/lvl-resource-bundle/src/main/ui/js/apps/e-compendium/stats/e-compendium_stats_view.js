/**
 * RequireJS module that defines the view: e-compendium->stats.
 */

define([ 'marionette', 'tpl!apps/e-compendium/stats/templates/e-compendium_stats', 'flatui-checkbox', 'flatui-radio' ], function(Marionette, StatsTpl) {
    return {
        Content : Marionette.ItemView.extend({
            id : 'stats',
            template : StatsTpl
        })
    };
});