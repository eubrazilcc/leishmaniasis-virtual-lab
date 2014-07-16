/**
 * RequireJS module that defines the controller: header->home.
 */

define([ 'app', 'apps/header/show/home_header_view' ], function(Lvl, View) {

    Lvl.module('HeaderApp.Home', function(Home, Lvl, Backbone, Marionette, $, _) {
        'use strict';
        Home.Controller = {
            showHeader : function() {
                require([ 'entities/navigation' ], function() {
                    var links = Lvl.request('navigation:external:entities');

                    // TODO
                    console.log(JSON.stringify(links));
                    // TODO

                    var view = new View.Header({
                        collection : links
                    });


                    Lvl.headerRegion.show(view);
                    return View.Header.id;
                });
            }
        }
    });

    return Lvl.HeaderApp.Home.Controller;
});