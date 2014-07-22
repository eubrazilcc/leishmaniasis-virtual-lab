/**
 * RequireJS module that defines the view: header->home.
 */

define([ 'marionette', 'tpl!apps/header/show/templates/header_home', 'flatui-checkbox', 'flatui-radio' ], function(Marionette, HomeHeaderTpl) {
    return {
        Header : Marionette.ItemView.extend({
            id : 'home',
            template : HomeHeaderTpl
        })
    };
});