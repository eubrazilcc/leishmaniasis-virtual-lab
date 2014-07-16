/**
 * RequireJS module that defines the sub-application: header.
 */

define([ 'app' ], function(Lvl) {

    Lvl.module('HeaderApp', function(HeaderApp, Lvl, Backbone, Marionette, $, _) {
        'use strict';
        HeaderApp.startWithParent = true;

        HeaderApp.onStart = function() {
            console.log('starting HeaderApp');
        };

        HeaderApp.onStop = function() {
            console.log('stopping HeaderApp');
        };

        HeaderApp.currentHeader = null;

        Lvl.commands.setHandler('set:active:header', function(id, section) {
            id = id || 'default';
            if (HeaderApp.currentHeader !== id) {
                if (id === 'home') {
                    require([ 'apps/header/show/home_header_ctrl' ], function(HomeHeaderCtrl) {                        
                        HeaderApp.currentHeader = HomeHeaderCtrl.showHeader();
                    });
                } else if (id === 'workspace') {
                    // TODO
                } else {
                    Lvl.headerRegion.reset();
                    HeaderApp.currentHeader = null;
                }
            }
        });
    });

    return Lvl.HeaderApp;
});