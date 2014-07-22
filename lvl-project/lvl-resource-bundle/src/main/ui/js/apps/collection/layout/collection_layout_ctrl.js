/**
 * RequireJS module that defines the controller: collection->layout.
 */

define([ 'app', 'apps/collection/layout/collection_layout_view' ], function(Lvl, View) {
    Lvl.module('CollectionApp.Layout', function(Layout, Lvl, Backbone, Marionette, $, _) {
        'use strict';
        Layout.Controller = {
            showLayout : function(section) {
                require([ 'apps/collection/' + section + '/collection_' + section + '_ctrl', 'apps/collection/layout/entities/tablinks' ], function(
                        SectionController) {
                    var tabLinks = Lvl.request('collection:navigation:entities');
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
    return Lvl.CollectionApp.Layout.Controller;
});