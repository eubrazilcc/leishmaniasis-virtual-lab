/**
 * RequireJS module that defines the sub-application: login.
 */

define([ 'app' ], function(Lvl) {
    Lvl.module('LoginApp', function(LoginApp, Lvl, Backbone, Marionette, $, _) {
        'use strict';

        /* Initialization & finalization */
        LoginApp.startWithParent = false;

        LoginApp.onStart = function() {
            console.log('starting LoginApp');
        };

        LoginApp.onStop = function() {
            console.log('stopping LoginApp');
        };

        /* Commands and events */
        Lvl.commands.setHandler('show:login', function(target, reason) {
            require([ 'apps/login/show/login_show_ctrl' ], function(ShowController) {
                ShowController.showLogin(target, reason);
            });
        });

        Lvl.commands.setHandler('show:registration', function() {
            require([ 'apps/login/register/login_register_ctrl' ], function(RegisterController) {
                RegisterController.register();
            });
        });

        Lvl.commands.setHandler('show:account:validation', function() {
            require([ 'apps/login/account-validation/account-validation_ctrl' ], function(AccountController) {
                AccountController.validate();
            });
        });
    });
});