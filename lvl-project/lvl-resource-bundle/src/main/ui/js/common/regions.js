/**
 * RequireJS module that defines regions common to all the application.
 */

define([ 'app' ], function(Lvl) {
    Lvl.module('Common.Regions', function(Regions, Lvl, Backbone, Marionette, $, _) {
        Regions.FadeInRegion = Marionette.Region.extend({
            open : function(view) {
                this.$el.hide();
                this.$el.html(view.el);
                this.$el.fadeIn();
            }
        });
    });
    return Lvl.Common.Regions;
});