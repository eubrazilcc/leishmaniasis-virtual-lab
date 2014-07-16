/**
 * RequireJS module that defines the sub-application: footer.
 */

define([ 'app' ], function(Lvl) {

    Lvl.module('FooterApp', function(FooterApp, Lvl, Backbone, Marionette, $, _) {
        'use strict';
        FooterApp.startWithParent = true;

        FooterApp.onStart = function() {
            console.log('starting FooterApp');
        };

        FooterApp.onStop = function() {
            console.log('stopping FooterApp');
        };

        FooterApp.currentFooter = null;

        Lvl.commands.setHandler('set:active:footer', function(id) {
            id = id || 'default';
            if (FooterApp.currentFooter !== id) {
                if (id === 'no_footer') {
                    Lvl.footerRegion.reset();
                    FooterApp.currentFooter = null;
                } else {
                    require([ 'apps/footer/show/show_footer_ctrl' ], function(FooterCtrl) {                        
                        FooterApp.currentFooter = FooterCtrl.showFooter();
                    });
                }
            }
        });
    });

    return Lvl.FooterApp;
});