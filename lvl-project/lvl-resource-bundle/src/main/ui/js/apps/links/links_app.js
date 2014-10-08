/**
 * RequireJS module that defines the sub-application: links.
 */

define([ 'app' ], function(Lvl) {
	Lvl.module('LinksApp', function(LinksApp, Lvl, Backbone, Marionette, $, _) {
        'use strict';

        /* Initialization & finalization */
        LinksApp.startWithParent = false;

        LinksApp.onStart = function() {
            console.log('starting LinksApp');
        };

        LinksApp.onStop = function() {
            console.log('stopping LinksApp');
        };

        /* Commands and events */
        Lvl.commands.setHandler('show:links', function() {
            require([ 'apps/links/show/links_show_ctrl' ], function(ShowController) {
                ShowController.showLinks();
            });
        });
    });
});