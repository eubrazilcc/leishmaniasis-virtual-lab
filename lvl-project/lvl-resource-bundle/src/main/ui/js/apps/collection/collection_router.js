/**
 * RequireJS module that defines the routes of the sub-application: collection.
 */

define([ 'app', 'routefilter' ], function(Lvl) {

    Lvl.module('Routers.CollectionApp', function(CollectionAppRouter, Lvl, Backbone, Marionette, $, _) {
        'use strict';

        var Router = Backbone.Router.extend({
            routes : {
                'collection' : 'browseCollection'
            },
            before : function() {
                require([ 'apps/collection/collection_app' ], function() {
                    Lvl.execute('set:active:header', 'workspace');
                    Lvl.execute('set:active:footer', 'workspace');
                    Lvl.startSubApp('CollectionApp');
                });
            },
            browseCollection : function() {
                Lvl.execute('browse:collection');
            }
        });

        Lvl.addInitializer(function() {
            var router = new Router();
        });
    });

});