/**
 * RequireJS module that defines the view: home->show.
 */

define([ 'marionette', 'tpl!apps/home/show/templates/home', 'flatui-checkbox', 'flatui-radio' ], function(Marionette, HomeTpl) {

    return {
        Content : Marionette.ItemView.extend({
            template : HomeTpl            
        })
    };

});