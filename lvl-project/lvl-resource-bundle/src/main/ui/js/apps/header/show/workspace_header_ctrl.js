/**
 * RequireJS module that defines the controller: header->workspace.
 */

define([ 'app', 'apps/header/show/workspace_header_view' ], function(Lvl, View) {
    Lvl.module('HeaderApp.Workspace', function(Workspace, Lvl, Backbone, Marionette, $, _) {
        'use strict';
        Workspace.Controller = {
            showHeader : function() {
                require([ 'entities/navigation' ], function() {
                    var links = Lvl.request('navigation:external:entities');
                    var view = new View.Header({
                        collection : links
                    });
                    Lvl.headerRegion.show(view);
                    return View.Header.id;
                });
            }
        }
    });
    return Lvl.HeaderApp.Workspace.Controller;
});