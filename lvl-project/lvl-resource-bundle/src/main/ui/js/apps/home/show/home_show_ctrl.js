/**
 * RequireJS module that defines the controller: home->show.
 */

define([ 'app', 'apps/home/show/home_show_view', 'apps/config/marionette/configuration' ], function(Lvl, View, Configuration) {
    Lvl.module('HomeApp.Show', function(Show, Lvl, Backbone, Marionette, $, _) {
        'use strict';
        var lvlEndpoint = new Configuration().get('endpoint', '');
        Show.Controller = {
            showHome : function() {
                var view = new View.Content({
                    model : new Backbone.Model({
                        endpoint : lvlEndpoint
                    })
                });
                Lvl.mainRegion.show(view);
            }
        }
    });
    return Lvl.HomeApp.Show.Controller;
});