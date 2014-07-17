/**
 * RequireJS module that defines the LVL main application.
 */

define([ 'marionette', 'apps/config/marionette/regions/dialog' ], function(Marionette) {
    'use strict';
    var Lvl = new Marionette.Application();

    Lvl.addRegions({
        headerRegion : '#header-region',
        mainRegion : '#main-region',
        dialogRegion : Marionette.Region.Dialog.extend({
            el : '#dialog-region'
        }),
        footerRegion : '#footer-region'
    });

    Lvl.navigate = function(route, options) {
        options || (options = {});
        Backbone.history.navigate(route, options);
    };

    Lvl.getCurrentRoute = function() {
        return Backbone.history.fragment
    };

    Lvl.startSubApp = function(appName, args) {
        var currentApp = appName ? Lvl.module(appName) : null;
        if (Lvl.currentApp === currentApp) {
            return;
        }
        if (Lvl.currentApp) {
            Lvl.currentApp.stop();
        }
        Lvl.currentApp = currentApp;
        if (currentApp) {
            currentApp.start(args);
        }
    };

    var History = Backbone.History.extend({
        loadUrl : function() {
            var match = Backbone.History.prototype.loadUrl.apply(this, arguments);
            if (!match) {
                if (Lvl.getCurrentRoute() !== '') {
                    require([ 'apps/not_found/not_found_app' ], function() {
                        Lvl.execute('set:active:header', 'home');
                        Lvl.execute('set:active:footer', 'home');
                        Lvl.startSubApp('NotFoundApp');
                        Lvl.execute('show:not_found');
                    });
                }
            }
            return match;
        }
    });

    Lvl.on('start', function() {
        if (Backbone.history) {
            Backbone.history instanceof History || (Backbone.history = new History());
            require([ 'apps/header/header_app', 'apps/footer/footer_app', 'apps/home/home_router', 'apps/login/login_router',
                    'apps/collection/collection_router' ], function() {
                Backbone.history.start();
                if (Lvl.getCurrentRoute() === '') {
                    Lvl.navigate('home', {
                        trigger : true
                    });
                }
            });
        }
    });

    return Lvl;
});