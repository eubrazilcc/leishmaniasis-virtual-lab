/**
 * RequireJS module that defines the view: header->workspace.
 */

define([ 'app', 'tpl!apps/header/show/templates/header_workspace', 'tpl!apps/header/show/templates/header_nav',
		'tpl!apps/header/show/templates/header_nav_link', 'tpl!apps/header/show/templates/header_notifications', 'apps/config/marionette/styles/style',
		'apps/config/marionette/configuration', 'moment', 'qtip' ], function(Lvl, WorkspaceHeaderTpl, NavigationTpl, NavigationLinkTpl, NotificationsTpl,
		Style, Configuration, moment) {
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

				this.$('#btnAlerts').click(function(event) {
					event.preventDefault();
				}).qtip({
					content : {
						text : function(event, api) {
							api.elements.content.html('<img src="/img/ajax_loader_gray_32.gif" alt="Loading..."/>');
							return $.ajax({
								url : config.get('service', '') + '/notifications',
								type : 'GET',
								headers : config.authorizationHeader(),
								dataType : 'json'
							}).then(function(content) {
								var tplData = {
									notifications : null
								};
								if (content && content.elements) {
									content.elements.sort(function(a, b) {
										return b.date - a.date;
									});
									var notifications = [];
									for (i = 0; i < content.elements.length && i < 5; i++) {
										var msg = content.elements[i].message || '';
										notifications.push({
											date : moment(content.elements[i].issuedAt).format('MMM DD[,] YYYY [at] HH[:]mm'),
											message : (msg.length > 24 ? msg.substr(0, 23) + '&hellip;' : msg)
										});
									}
									tplData = {
										'notifications' : notifications
									};
								}
								// console.log('DEBUG_NOTIF: ' + NotificationsTpl(tplData));
								return NotificationsTpl(tplData);
							}, function(xhr, status, error) {
								api.set('content.text', status + ': ' + error);
							});
						}
					},
					style : {
						classes : 'qtip-bootstrap lvl-notifications-container'
					},
					show : 'click',
					hide : 'unfocus',
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