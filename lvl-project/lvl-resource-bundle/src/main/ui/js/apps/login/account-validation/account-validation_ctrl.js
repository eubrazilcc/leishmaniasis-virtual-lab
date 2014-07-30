/**
 * RequireJS module that defines the controller: login->account-validation.
 */

define([ 'app', 'apps/login/account-validation/account-validation_view' ], function(Lvl, View) {
    Lvl.module('LoginApp.AccountValidation', function(AccountValidation, Lvl, Backbone, Marionette, $, _) {
        'use strict';
        AccountValidation.Controller = {
            validate : function() {
                var view = new View.Content();
                Lvl.mainRegion.show(view);
            }
        }
    });
    return Lvl.LoginApp.AccountValidation.Controller;
});