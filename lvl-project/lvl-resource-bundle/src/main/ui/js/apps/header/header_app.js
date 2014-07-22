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
                    require([ 'apps/header/show/header_home_ctrl' ], function(HomeHeaderCtrl) {
                        HeaderApp.currentHeader = HomeHeaderCtrl.showHeader();
                    });
                } else if (id === 'workspace') {
                    require([ 'apps/header/show/header_workspace_ctrl' ], function(WorkspaceHeaderCtrl) {
                        HeaderApp.currentHeader = WorkspaceHeaderCtrl.showHeader();
                    });
                } else {
                    Lvl.headerRegion.reset();
                    HeaderApp.currentHeader = null;
                }
            }
        });
    });
    return Lvl.HeaderApp;
});