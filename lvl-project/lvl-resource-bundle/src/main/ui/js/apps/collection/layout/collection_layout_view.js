/**
 * RequireJS module that defines the view: collection->layout.
 */

define([ 'app', 'apps/config/marionette/regions/fadein', 'tpl!apps/collection/layout/templates/collection-layout',
        'tpl!apps/collection/layout/templates/tab-list', 'tpl!apps/collection/layout/templates/tab-link', 'flatui-checkbox', 'flatui-radio' ], function(Lvl,
        FadeInRegion, LayoutTpl, TabListTpl, TabLinkTpl) {
    Lvl.module('CollectionApp.Layout.View', function(View, Lvl, Backbone, Marionette, $, _) {
        View.TabLink = Marionette.ItemView.extend({
            tagName : 'li',
            template : TabLinkTpl,
            onRender : function() {
                if (this.model.selected) {
                    this.$el.addClass('active');
                }
            }
        });
        View.TabList = Marionette.CompositeView.extend({
            template : TabListTpl,
            itemView : View.TabLink,
            appendHtml : function(collectionView, itemView) {
                collectionView.$('ul').append(itemView.el);
            }
        });
        View.Layout = Marionette.Layout.extend({
            template : LayoutTpl,
            regions : {
                tabList : '#section-tab-list',
                tabContent : FadeInRegion.extend({
                    el : '#section-tab-content'
                })
            },
            initialize : function(options) {
                this.navLinks = options.navigation;
            },
            onRender : function(options) {
                this.tabList.show(new View.TabList({
                    collection : options.navLinks
                }));
            }
        });
    });
    return Lvl.CollectionApp.Layout.View;
});