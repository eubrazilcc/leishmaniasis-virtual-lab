/**
 * RequireJS module that defines the controller: not-found->show.
 */

define([ 'app', 'apps/not_found/show/show_not_found_view' ], function(Lvl, View) {

    Lvl.module('NotFoundApp.Show', function(Show, Lvl, Backbone, Marionette, $, _) {
        'use strict';
        Show.Controller = {
            showNotFound : function() {
                var view = new View.Content();
                Lvl.mainRegion.show(view);
            }
        }
    });

    return Lvl.NotFoundApp.Show.Controller;
});