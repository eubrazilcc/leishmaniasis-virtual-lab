/**
 * RequireJS module that defines the controller: e-compendium->layout.
 */

define([ 'app', 'apps/e-compendium/layout/e-compendium_layout_view' ], function(Lvl, View) {
    Lvl.module('ECompendiumApp.Layout', function(Layout, Lvl, Backbone, Marionette, $, _) {
        'use strict';
        Layout.Controller = {
            showLayout : function(section) {
                require([ 'apps/e-compendium/' + section + '/e-compendium_' + section + '_ctrl', 'apps/e-compendium/layout/entities/tablinks' ], function(
                        SectionController) {
                    var tabLinks = Lvl.request('e-compendium:navigation:entities');
                    var tabLinkToSelect = tabLinks.find(function(tabLink) {
                        return tabLink.get('link') === section;
                    });
                    tabLinkToSelect.select();
                    tabLinks.trigger('reset');
                    var view = new View.Layout({
                        navigation : tabLinks
                    });
                    Lvl.mainRegion.show(view);
                    return SectionController.showSection();
                });
            }
        }
    });
    return Lvl.ECompendiumApp.Layout.Controller;
});