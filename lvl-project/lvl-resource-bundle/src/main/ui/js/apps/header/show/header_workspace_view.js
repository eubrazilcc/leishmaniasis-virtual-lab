/**
 * RequireJS module that defines the view: header->workspace.
 */

define([ 'app', 'tpl!apps/header/show/templates/header_workspace', 'tpl!apps/header/show/templates/header_nav', 'flatui-checkbox', 'flatui-radio' ], function(
        Lvl, WorkspaceHeaderTpl, NavigationTpl) {
    Lvl.module('HeaderApp.Workspace.View', function(View, Lvl, Backbone, Marionette, $, _) {
        View.Navigation = Marionette.ItemView.extend({
            template : NavigationTpl
        });
        View.Header = Marionette.Layout.extend({
            id : 'workspace',
            template : WorkspaceHeaderTpl,
            regions : {
                navigation : '#section-navigation'
            },
            initialize : function(options) {
                this.navLinks = options.navigation;
            },
            onRender : function(options) {
                this.navigation.show(new View.Navigation({
                    collection : options.navLinks
                }));
            }
        });
    });
    return Lvl.HeaderApp.Workspace.View;
});