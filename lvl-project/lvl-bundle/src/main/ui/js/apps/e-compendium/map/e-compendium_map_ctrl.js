/**
 * RequireJS module that defines the controller: e-compendium->map.
 */

define([ 'app', 'apps/e-compendium/map/e-compendium_map_view' ], function(Lvl, View) {
    Lvl.module('CollectionApp.Map', function(Map, Lvl, Backbone, Marionette, $, _) {
        'use strict';
        Map.Controller = {
            showSection : function() {
                var view = new View.Content();
                Lvl.mainRegion.currentView.tabContent.show(view);
                return View.Content.id;
            }
        }
    });
    return Lvl.CollectionApp.Map.Controller;
});