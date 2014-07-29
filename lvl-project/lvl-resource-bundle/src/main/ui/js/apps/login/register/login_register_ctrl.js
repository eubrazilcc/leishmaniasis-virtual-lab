/**
 * RequireJS module that defines the controller: login->register.
 */

define([ 'app', 'apps/login/register/login_register_view' ], function(Lvl, View) {
    Lvl.module('LoginApp.Register', function(Register, Lvl, Backbone, Marionette, $, _) {
        'use strict';
        Register.Controller = {
            register : function() {
                var view = new View.Content();
                Lvl.mainRegion.show(view);
            }
        }
    });
    return Lvl.LoginApp.Register.Controller;
});