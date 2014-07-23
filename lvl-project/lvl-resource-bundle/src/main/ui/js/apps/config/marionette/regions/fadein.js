/**
 * RequireJS module that defines a region common to all the application that
 * uses a dissolve transition.
 */

define([ 'marionette', 'jquery-ui' ], function(Marionette) {
    Marionette.Region.FadeInRegion = Marionette.Region.extend({
        open : function(view) {
            this.$el.hide();
            this.$el.html(view.el);
            this.$el.fadeIn();
        }
    });
    return Marionette.Region.FadeInRegion;
});