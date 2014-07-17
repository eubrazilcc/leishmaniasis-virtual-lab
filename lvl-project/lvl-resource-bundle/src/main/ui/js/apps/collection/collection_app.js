/**
 * RequireJS module that defines the sub-application: collection.
 */

define([ 'app' ], function(Lvl) {

    Lvl.module('CollectionApp', function(CollectionApp, Lvl, Backbone, Marionette, $, _) {
        'use strict';

        /* Initialization & finalization */
        CollectionApp.startWithParent = false;

        CollectionApp.onStart = function() {
            console.log('starting CollectionApp');
        };

        CollectionApp.onStop = function() {
            console.log('stopping CollectionApp');
        };

        /* Commands and events */
        Lvl.commands.setHandler('browse:collection', function() {
            require([ 'apps/collection/browse/browse_collection_ctrl' ], function(BrowseController) {
                BrowseController.browseCollection();
            });
        });

    });

});