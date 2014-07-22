/**
 * RequireJS module that defines the view: collection->map.
 */

define([ 'marionette', 'tpl!apps/collection/map/templates/collection_map', 'flatui-checkbox', 'flatui-radio' ], function(Marionette, MapTpl) {
    return {
        Content : Marionette.ItemView.extend({
            id : 'map',
            template : MapTpl
        })
    };
});