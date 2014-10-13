/**
 * RequireJS module that defines the view: e-compendium->map.
 */

define([ 'marionette', 'tpl!apps/e-compendium/map/templates/e-compendium_map', 'flatui-checkbox', 'flatui-radio' ], function(Marionette, MapTpl) {
    return {
        Content : Marionette.ItemView.extend({
            id : 'map',
            template : MapTpl
        })
    };
});