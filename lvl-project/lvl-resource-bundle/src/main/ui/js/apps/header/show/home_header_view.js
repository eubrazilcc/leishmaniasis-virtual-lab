/**
 * RequireJS module that defines the view: header->home.
 */

define([ 'marionette', 'tpl!apps/header/show/templates/home_header', 'flatui-checkbox', 'flatui-radio' ], function(Marionette, HomeHeaderTpl) {

    return {
        Header : Marionette.ItemView.extend({
            id : 'home',
            template : HomeHeaderTpl            
        })
    };

});