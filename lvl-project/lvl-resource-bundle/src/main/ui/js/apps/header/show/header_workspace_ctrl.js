/**
 * RequireJS module that defines the controller: header->workspace.
 */

define([ 'app', 'apps/header/show/header_workspace_view' ], function(Lvl, View) {
    Lvl.module('HeaderApp.Workspace', function(Workspace, Lvl, Backbone, Marionette, $, _) {
        'use strict';
        Workspace.Controller = {
            showHeader : function() {
                require([ 'entities/navigation' ], function() {
                    var view = new View.Header({
                        navigation : Lvl.request('navigation:links+settings:entities')
                    });
                    Lvl.headerRegion.show(view);
                    return View.Header.id;
                });
            }
        }
    });
    return Lvl.HeaderApp.Workspace.Controller;
});