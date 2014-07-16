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
        Lvl.commands.setHandler('show:login', function() {
            require([ 'apps/login/show/show_login_ctrl' ], function(ShowController) {
                ShowController.showLogin();
            });
        });
    });

});