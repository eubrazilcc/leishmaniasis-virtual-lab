/**
 * RequireJS module that defines the controller: access->register.
 */

define([ 'app', 'apps/access/register/access_register_view' ], function(Lvl, View) {
    Lvl.module('AccessApp.Register', function(Register, Lvl, Backbone, Marionette, $, _) {
        'use strict';
        Register.Controller = {
            register : function() {
                var view = new View.Content();
                Lvl.mainRegion.show(view);
            }
        }
    });
    return Lvl.AccessApp.Register.Controller;
});