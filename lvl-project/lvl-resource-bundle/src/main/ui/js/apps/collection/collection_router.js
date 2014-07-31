/**
 * RequireJS module that defines the routes of the sub-application: collection.
 */

define([ 'app', 'routefilter' ], function(Lvl) {
    Lvl.module('Routers.CollectionApp', function(CollectionAppRouter, Lvl, Backbone, Marionette, $, _) {
        'use strict';
        var Router = Backbone.Router.extend({
            routes : {
                'collection' : 'defaultCollection',
                'collection/browse' : 'browseCollection',
                'collection/map' : 'mapCollection',
                'collection/stats' : 'statsCollection',
                'collection/submit' : 'submitCollection'
            },
            before : function() {
                require([ 'apps/collection/collection_app' ], function() {
                    Lvl.execute('set:active:header', 'workspace', 'collection');
                    Lvl.execute('set:active:footer', 'workspace');
                    Lvl.startSubApp('CollectionApp');
                });
            },
            defaultCollection : function() {
                var self = this;
                Lvl.navigate('collection/browse', {
                    trigger : true
                });
            },
            browseCollection : function() {
                Lvl.execute('collection:set:active', 'browse');
            },
            mapCollection : function() {
                Lvl.execute('collection:set:active', 'map');
            },
            statsCollection : function() {
                Lvl.execute('collection:set:active', 'stats');
            },
            submitCollection : function() {
                Lvl.execute('collection:set:active', 'submit');
            }
        });

        Lvl.addInitializer(function() {
            var router = new Router();
        });
    });
});