/**
 * RequireJS module that defines the routes of the sub-application: login.
 */

define([ 'app', 'routefilter' ], function(Lvl) {
    Lvl.module('Routers.LoginApp', function(LoginAppRouter, Lvl, Backbone, Marionette, $, _) {
        'use strict';
        var Router = Backbone.Router.extend({
            routes : {
                'login' : 'showLogin',
                'register' : 'showRegistration',
                'account/validation' : 'validateAccount'
            },
            before : function() {
                require([ 'apps/login/login_app' ], function() {
                    Lvl.execute('set:active:header', 'no_header');
                    Lvl.execute('set:active:footer', 'no_footer');
                    Lvl.startSubApp('LoginApp');
                });
            },
            showLogin : function() {
                Lvl.execute('show:login');
            },
            showRegistration : function() {
                Lvl.execute('show:registration');
            },
            validateAccount : function() {
                Lvl.execute('show:account:validation');
            }
        });
        Lvl.addInitializer(function() {
            var router = new Router();
        });
    });
});