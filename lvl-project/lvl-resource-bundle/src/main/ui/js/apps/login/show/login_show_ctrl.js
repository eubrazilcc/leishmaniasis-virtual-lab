/**
 * RequireJS module that defines the controller: login->show.
 */

define([ 'app', 'apps/login/show/login_show_view' ], function(Lvl, View) {
    Lvl.module('LoginApp.Show', function(Show, Lvl, Backbone, Marionette, $, _) {
        'use strict';
        Show.Controller = {
            showLogin : function(target, reason) {
                var view = new View.Content({
                    model : new Backbone.Model({
                        'target' : target,
                        'reason' : reason
                    })
                });
                Lvl.mainRegion.show(view);
            }
        }
    });
    return Lvl.LoginApp.Show.Controller;
});