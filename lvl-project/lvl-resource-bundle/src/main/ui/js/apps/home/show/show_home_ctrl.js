/**
 * RequireJS module that defines the controller: home->show.
 */

define([ 'app', 'apps/home/show/show_home_view' ], function(Lvl, View) {

    Lvl.module('HomeApp.Show', function(Show, Lvl, Backbone, Marionette, $, _) {
        'use strict';
        Show.Controller = {
            showHome : function() {
                var view = new View.Content();
                Lvl.mainRegion.show(view);
            }
        }
    });

    return Lvl.HomeApp.Show.Controller;
});