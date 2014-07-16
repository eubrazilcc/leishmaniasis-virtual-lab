/**
 * RequireJS module that defines the controller: login->show.
 */

define([ 'app', 'apps/login/show/show_login_view' ], function(Lvl, View) {

    Lvl.module('LoginApp.Show', function(Show, Lvl, Backbone, Marionette, $, _) {
        'use strict';
        Show.Controller = {
            showLogin : function() {
                var view = new View.Content();                
                Lvl.mainRegion.show(view);
            }
        }
    });

    return Lvl.LoginApp.Show.Controller;
});