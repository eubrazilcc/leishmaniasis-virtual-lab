/**
 * RequireJS module that defines the sub-application: files.
 */

define([ 'app' ], function(Lvl) {
	Lvl.module('FilesApp', function(FilesApp, Lvl, Backbone, Marionette, $, _) {
        'use strict';

        /* Initialization & finalization */
        FilesApp.startWithParent = false;

        FilesApp.onStart = function() {
            console.log('starting FilesApp');
        };

        FilesApp.onStop = function() {
            console.log('stopping FilesApp');
        };
        
        FilesApp.currentSection = null;

        /* Commands and events */
        Lvl.commands.setHandler('files:set:active', function(section) {
        	section = section || 'default';
            if (FilesApp.currentSection !== section) {
                if (section === 'links') {
                    require([ 'apps/files/layout/files_layout_ctrl' ], function(LayoutController) {
                    	FilesApp.currentSection = LayoutController.showLayout(section);
                    });
                } else {
                    Lvl.mainRegion.currentView.reset();
                    FilesApp.currentSection = null;
                }
            }        	
        });
    });
});