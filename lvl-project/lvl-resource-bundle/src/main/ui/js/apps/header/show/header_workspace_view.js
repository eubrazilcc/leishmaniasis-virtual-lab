/**
 * RequireJS module that defines the view: header->workspace.
 */

define([ 'app', 'tpl!apps/header/show/templates/header_workspace', 'tpl!apps/header/show/templates/header_nav',
		'tpl!apps/header/show/templates/header_nav_link', 'apps/config/marionette/styles/style', 'apps/config/marionette/configuration', 'qtip',
		'flatui-checkbox', 'flatui-radio' ], function(Lvl, WorkspaceHeaderTpl, NavigationTpl, NavigationLinkTpl, Style, Configuration) {
	Lvl.module('HeaderApp.Workspace.View', function(View, Lvl, Backbone, Marionette, $, _) {
		var config = new Configuration();
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
			onBeforeRender : function() {
				require([ 'entities/styles' ], function() {
					var stylesLoader = new Style();
					stylesLoader.loadCss(Lvl.request('styles:qtip:entities').toJSON());
				});
			},
			onRender : function(options) {
				this.navigation.show(new View.Navigation({
					model : options.navLinks.selected || options.navLinks.at(0),
					collection : options.navLinks
				}));
				this.$('#btnAlerts').qtip({
					content : {
						text : function(event, api) {
							api.elements.content.html('<img src="/img/ajax_loader_gray_32.gif" alt="Loading..."/>');
							return $.ajax({
								url : config.get('service', '') + '/notifications',
								type: 'GET',
								headers : config.authorizationHeader(),
								dataType: 'json'
							}).then(function(content) {
								
								// TODO
								
								return content;
							}, function(xhr, status, error) {
								api.set('content.text', status + ': ' + error);
							});
						}
					},
					style : {
						classes : 'qtip-bootstrap lvl-notifications-container'
					},
					hide : {
						fixed : true,
						delay : 300
					},
					position : {
						my : 'top center',
						at : 'bottom center'
					}
				});
			}
		});
	});
	return Lvl.HeaderApp.Workspace.View;
});