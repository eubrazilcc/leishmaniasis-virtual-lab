/**
 * RequireJS module that defines the view: header->home.
 */

define([ 'app', 'tpl!apps/header/show/templates/header_home', 'flatui-checkbox', 'flatui-radio' ], function(Lvl, HomeHeaderTpl) {
    Lvl.module('HeaderApp.Home.View', function(View, Lvl, Backbone, Marionette, $, _) {
        View.id = 'home';
        View.Header = Marionette.ItemView.extend({
            template : HomeHeaderTpl,
            templateHelpers : {
                sessionLink : function() {
                    return encodeURIComponent(this.sessionLink);
                },
                sessionText : function() {
                    return this.sessionText;
                }
            }
        });
    });
    return Lvl.HeaderApp.Home.View;
});