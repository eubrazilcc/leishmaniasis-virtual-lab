/**
 * RequireJS module that defines the view: header->workspace.
 */

define([ 'app', 'tpl!apps/header/show/templates/header_workspace', 'tpl!apps/header/show/templates/header_nav',
		'tpl!apps/header/show/templates/header_nav_link', 'tpl!apps/header/show/templates/header_notifications', 'apps/config/marionette/styles/style',
		'apps/config/marionette/configuration', 'moment', 'qtip' ], function(Lvl, WorkspaceHeaderTpl, NavigationTpl, NavigationLinkTpl, NotificationsTpl,
		Style, Configuration, moment) {
	Lvl.module('HeaderApp.Workspace.View', function(View, Lvl, Backbone, Marionette, $, _) {
		var config = new Configuration();
		function openSearchForm() {
			var searchBox = $('#lvl-search-box');
			if (!searchBox.is(':visible')) {
				searchBox.show('fast', function() {
					$('#lvl-search-form-input').focus();
				});
			}
		}
		function closeSearchForm(duration) {
			var searchBox = $('#lvl-search-box');
			if (searchBox.is(':visible')) {
				if (duration === undefined)
					duration = 'fast';
				searchBox.hide(duration);
			}
		}
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
			childView : View.NavigationLink,
			childViewContainer : 'ul',
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
		View.Header = Marionette.LayoutView.extend({
			template : WorkspaceHeaderTpl,
			templateHelpers : {
				username : function() {
					$.ajax({
						type : 'GET',
						url : config.get('auth') + '/users/' + config.session.get('user.session').email + "?use_email=true",
						headers : config.authorizationHeader(),
						dataType : 'json'
					}).done(function(data, textStatus, request) {
						if (data.firstname) {
							$('#username').html(function(idx, oldhtml) {
								return oldhtml.replace(/Noname/, data.firstname);
							});
						}
					});
					return 'Noname';
				}
			},
			events : {
				'click a#btnSearchToggle' : 'toggleSearchForm',
				'click a#btnProfile' : 'showUserProfile',
				'click a#lvl-search-form-collapse-btn' : 'collapseSearchForm',
				'click button#lvl-search-form-submit-btn-xs' : 'submitSearchFormXs',
				'submit form#lvl-search-form' : 'submitSearchForm',
				'dragover div#lvl-save-items-target' : 'saveItemsDragOverHandler',
				'dragenter div#lvl-save-items-target' : 'saveItemsDragEnterHandler',
				'dragleave div#lvl-save-items-target' : 'saveItemsDragLeaveHandler',
				'drop div#lvl-save-items-target' : 'saveItemsDropHandler'
			},
			toggleSearchForm : function(e) {
				e.preventDefault();
				if ($('#lvl-search-box').is(':visible')) {
					closeSearchForm();
				} else {
					openSearchForm();
				}
			},
			collapseSearchForm : function(e) {
				e.preventDefault();
				closeSearchForm();
			},
			handleEscKeyUpEvent : function(e) {
				if (e.which == 27 && $('#lvl-search-box').is(':visible')) {
					closeSearchForm();
				}
			},
			showUserProfile : function(e) {
				e.preventDefault();
				this.trigger('access:user:profile');
			},
			submitSearchFormXs : function(e) {
				e.preventDefault();
				var searchInputXs = this.$('#lvl-search-form-input-xs');
				var searchInput = this.$('#lvl-search-form-input');
				searchInput.val(searchInputXs.val());
				this.$('#lvl-search-form').submit();
				searchInputXs.val('');
			},
			submitSearchForm : function(e) {
				e.preventDefault();
				var searchInput = this.$('#lvl-search-form-input');
				Lvl.vent.trigger('search:form:submitted', searchInput.val());
				searchInput.val('');
				closeSearchForm(0);
			},
			saveItemsDragOverHandler : function(e) {
				e.preventDefault();
				e.originalEvent.dataTransfer.dropEffect = 'copy';
				$('div#lvl-save-items-target').addClass('over');
				return false;
			},
			saveItemsDragEnterHandler : function(e) {
				$('div#lvl-save-items-target').addClass('over');
			},
			saveItemsDragLeaveHandler : function(e) {
				$('div#lvl-save-items-target').removeClass('over');
			},
			saveItemsDropHandler : function(e) {
				e.preventDefault();
				var srcId = e.originalEvent.dataTransfer.getData('srcId');
				var srcElem = $('div[data-savable-id="' + srcId + '"]');
				var savableType = e.originalEvent.dataTransfer.getData('savableType');
				var savable = e.originalEvent.dataTransfer.getData('savable');
				if (savableType && savable) {
					require([ 'entities/' + savableType ], function(SavableEntity) {
						switch (savableType) {
						case 'saved_search':
							var savableObj = new SavableEntity.SavedSearch(JSON.parse(savable));							

							// TODO
							console.log('SAVED: ' + JSON.stringify(savableObj.toJSON()));
							// TODO

							savableObj.save();
							break;
						default:
							console.error('Unsupported savable type ignored: ' + savableType);
							break;
						}
					});
				}
				srcElem.remove();
				console.log && console.log('dropped element: ' + srcElem.prop('tagName').toLowerCase() + ', with id: ' + srcId);				
				this.hideMySavedItems();
			},
			showMySavedItems : function() {
				$('div#lvl-save-items').show('fast');
			},
			hideMySavedItems : function() {
				$('div#lvl-save-items-target').removeClass('over');
				$('div#lvl-save-items').hide('fast');
			},
			regions : {
				navigation : '#section-navigation'
			},
			initialize : function(options) {
				this.navLinks = options.navigation;
				// setup items saving
				Lvl.vent.on('editable:items:dragstart', this.showMySavedItems);
				Lvl.vent.on('editable:items:dragend', this.hideMySavedItems);
				// subscribe to events
				$(document).on('keyup', this.handleEscKeyUpEvent);
			},
			onDestroy : function() {
				closeSearchForm(0);
				// remove all event handlers
				Lvl.vent.off('editable:items:dragstart');
				Lvl.vent.off('editable:items:dragend');
				$(document).off('keyup', this.handleEscKeyUpEvent);
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