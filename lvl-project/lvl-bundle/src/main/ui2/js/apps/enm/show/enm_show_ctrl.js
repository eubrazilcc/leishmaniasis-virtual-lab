/**
 * RequireJS module that defines the controller: enm->show.
 */

define([ 'app', 'apps/enm/show/enm_show_view' ], function(Lvl, View) {
    Lvl.module('EnmApp.Show', function(Show, Lvl, Backbone, Marionette, $, _) {
        'use strict';
        Show.Controller = {
            showEnm : function() {
                var view = new View.Content();
                Lvl.mainRegion.show(view);
            }
        }
    });
    return Lvl.EnmApp.Show.Controller;
});