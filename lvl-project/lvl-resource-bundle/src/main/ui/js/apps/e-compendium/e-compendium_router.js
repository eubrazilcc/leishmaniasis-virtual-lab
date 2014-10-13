/**
 * RequireJS module that defines the routes of the sub-application: e-compendium.
 */

define([ 'app', 'apps/config/marionette/configuration', 'routefilter' ], function(Lvl, Configuration) {
    Lvl.module('Routers.ECompendiumApp', function(ECompendiumAppRouter, Lvl, Backbone, Marionette, $, _) {
        'use strict';
        var config = new Configuration();
        var Router = Backbone.Router.extend({
            routes : {
                'e-compendium' : 'defaultECompendium',
                'e-compendium/browse' : 'browseECompendium',
                'e-compendium/map' : 'mapECompendium',
                'e-compendium/stats' : 'statsECompendium',
                'e-compendium/submit' : 'submitECompendium'
            },
            before : function() {
                if (!config.isAuthenticated()) {
                    Lvl.navigate('login/' + encodeURIComponent(Backbone.history.fragment) + '/unauthenticated', {
                        trigger : true,
                        replace : true
                    });
                    return false;
                }
                require([ 'apps/e-compendium/e-compendium_app' ], function() {
                    Lvl.execute('set:active:header', 'workspace', 'e-compendium');
                    Lvl.execute('set:active:footer', 'workspace');
                    Lvl.startSubApp('ECompendiumApp');
                });
                return true;
            },
            defaultECompendium : function() {
                var self = this;
                Lvl.navigate('e-compendium/browse', {
                    trigger : true,
                    replace : true
                });
            },
            browseECompendium : function() {
                Lvl.execute('e-compendium:set:active', 'browse');
            },
            mapECompendium : function() {
                Lvl.execute('e-compendium:set:active', 'map');
            },
            statsECompendium : function() {
                Lvl.execute('e-compendium:set:active', 'stats');
            },
            submitECompendium : function() {
                Lvl.execute('e-compendium:set:active', 'submit');
            }
        });
        Lvl.addInitializer(function() {
            var router = new Router();
        });
    });
});