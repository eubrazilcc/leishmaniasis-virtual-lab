/**
 * RequireJS module that defines the controller: social->show.
 */

define([ 'app', 'apps/social/show/social_show_view' ], function(Lvl, View) {
    Lvl.module('SocialApp.Show', function(Show, Lvl, Backbone, Marionette, $, _) {
        'use strict';
        Show.Controller = {
            showSocial : function() {
                var view = new View.Content();
                Lvl.mainRegion.show(view);
            }
        }
    });
    return Lvl.SocialApp.Show.Controller;
});