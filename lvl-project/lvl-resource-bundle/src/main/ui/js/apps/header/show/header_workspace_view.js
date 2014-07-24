/**
 * RequireJS module that defines the view: header->workspace.
 */

define([ 'app', 'tpl!apps/header/show/templates/header_workspace', 'tpl!apps/header/show/templates/header_nav',
        'tpl!apps/header/show/templates/header_nav_link', 'flatui-checkbox', 'flatui-radio' ], function(Lvl, WorkspaceHeaderTpl, NavigationTpl,
        NavigationLinkTpl) {
    Lvl.module('HeaderApp.Workspace.View', function(View, Lvl, Backbone, Marionette, $, _) {
        View.id = 'workspace';
        View.NavigationLink = Marionette.ItemView.extend({
            tagName : 'li',
            template : NavigationLinkTpl,
            onRender : function() {
                this.$el.attr('role', 'presentation');
                if (this.model.selected) {
                    this.$el.addClass('hidden');
                }
                if (this.model.get('isFirst') === 'settings') {
                    this.$el.prepend('<li role="presentation" class="divider"></li>');
                }
            }
        });
        View.Navigation = Marionette.CompositeView.extend({
            template : NavigationTpl,
            itemView : View.NavigationLink,
            appendHtml : function(collectionView, itemView) {
                collectionView.$('ul').append(itemView.el);
            },
            collectionEvents : {
                'reset' : 'render'
            },
            onBeforeRender : function() {
                var selectedNavLink = this.collection.selected || this.collection.at(0);
                this.model.set({
                    'selected_icon' : selectedNavLink.get('icon'),
                    'selected_text' : selectedNavLink.get('text')
                });
            }
        });
        View.Header = Marionette.Layout.extend({
            template : WorkspaceHeaderTpl,
            regions : {
                navigation : '#section-navigation'
            },
            initialize : function(options) {
                this.navLinks = options.navigation;
            },
            onRender : function(options) {
                this.navigation.show(new View.Navigation({
                    model : options.navLinks.selected || options.navLinks.at(0),
                    collection : options.navLinks
                }));
            }
        });
    });
    return Lvl.HeaderApp.Workspace.View;
});