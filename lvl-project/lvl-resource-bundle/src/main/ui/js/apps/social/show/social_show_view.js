/**
 * RequireJS module that defines the view: social->show.
 */

define([ 'marionette', 'tpl!apps/social/show/templates/social', 'flatui-checkbox', 'flatui-radio' ], function(Marionette, SocialTpl) {
    return {
        Content : Marionette.ItemView.extend({
            template : SocialTpl
        })
    };
});