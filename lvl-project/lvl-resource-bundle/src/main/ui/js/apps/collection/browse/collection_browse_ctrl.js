/**
 * RequireJS module that defines the controller: collection->browse.
 */

define([ 'app', 'apps/collection/browse/collection_browse_view' ], function(Lvl, View) {
    Lvl.module('CollectionApp.Browse', function(Browse, Lvl, Backbone, Marionette, $, _) {
        'use strict';
        Browse.Controller = {
            showSection : function() {
                var view = new View.Content();
                Lvl.mainRegion.currentView.tabContent.show(view);
                return View.Content.id;
            }
        }
    });
    return Lvl.CollectionApp.Browse.Controller;
});