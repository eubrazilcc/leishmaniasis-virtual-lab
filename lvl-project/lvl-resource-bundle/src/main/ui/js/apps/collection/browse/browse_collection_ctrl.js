/**
 * RequireJS module that defines the controller: collection->browse.
 */

define([ 'app', 'apps/collection/browse/browse_collection_view' ], function(Lvl, View) {

    Lvl.module('CollectionApp.Browse', function(Browse, Lvl, Backbone, Marionette, $, _) {
        'use strict';
        Browse.Controller = {
            browseCollection : function() {
                var view = new View.Content();
                Lvl.mainRegion.show(view);
            }
        }
    });

    return Lvl.CollectionApp.Browse.Controller;
});