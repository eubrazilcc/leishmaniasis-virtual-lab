/**
 * RequireJS module that defines the view: header->workspace.
 */

define([ 'app', 'tpl!apps/header/show/tpls/header_workspace', 'tpl!apps/header/show/tpls/header_nav', 'tpl!apps/header/show/tpls/header_nav_link',
		'tpl!apps/header/show/tpls/header_notifications', 'moment', 'qtip', 'imagesloaded' ], function(Lvl, WorkspaceHeaderTpl, NavigationTpl,
		NavigationLinkTpl, NotificationsTpl, moment) {
	Lvl.module('HeaderApp.Workspace.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
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
				if (this.model.get('text').toLowerCase() !== 'separator') {
					if (this.model.selected) {
						this.$el.addClass('hidden');
					}
				} else {
					this.$el.empty();
					this.$el.addClass('divider');
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
			templateHelpers : function() {
				$.ajax({
					type : 'GET',
					url : Lvl.config.get('auth.url') + '/users/' + Lvl.config.session.get('user.session').email + "?use_email=true",
					headers : Lvl.config.authorizationHeader(),
					dataType : 'json'
				}).done(function(data, textStatus, request) {
					if (data.firstname) {
						$('#username').html(function(idx, oldhtml) {
							return oldhtml.replace(/Noname/, data.firstname);
						});
					}
					if (data.roles && $.isArray(data.roles)) {
						if (_.contains(data.roles, 'admin')) {
							$('#sectionAdmin').removeClass('hidden');
							$('#sectionCuration').removeClass('hidden');
						}
						if (_.contains(data.roles, 'curator')) {
							$('#sectionCuration').removeClass('hidden');
						}						
					}
				});
				return {
					username : function() {
						return 'Noname';
					}
				}
			},
			events : {
				'click a#btnSearchToggle' : 'toggleSearchForm',
				'click a#btnProfile' : 'showUserProfile',
				'click a#lvl-search-form-collapse-btn' : 'collapseSearchForm',
				'click button#lvl-search-form-submit-btn-xs' : 'submitSearchFormXs',
				'submit form#lvl-search-form' : 'submitSearchForm',
				'submit form#lvl-search-form-xs' : 'submitSearchFormXs',
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
							savableObj.oauth2_token = Lvl.config.authorizationToken();
							savableObj.save({}, {
								success : function(model, resp, options) {
									require([ 'common/growl' ], function(createGrowl) {
										var anchor = $('<a>', {
											href : options.xhr.getResponseHeader('Location')
										})[0];
										var filename = anchor.pathname.substring(anchor.pathname.lastIndexOf('/') + 1);
										createGrowl('Search saved',
												'<a href="/#saved-items/searches"><i class="fa fa-arrow-circle-right fa-fw"></i> Saved searches</a>', false);
									});
								},
								error : function(model, resp, options) {
									require([ 'common/alert' ], function(alertDialog) {
										alertDialog('Error', 'Failed to save search.');
									});
								}
							});
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
				// add notification timer
				this.timer = setInterval(function() {					
					$.ajax({
						url : Lvl.config.get('service.url') + '/notifications/~/total/count',
						type : 'GET',
						headers : Lvl.config.authorizationHeader(),
						dataType : 'json'
					}).then(function(content) {
						if (content) {
							var count = content.totalCount || 0;
							$('#notificationsCount').text(count);							
						}
					}, function(xhr, status, error) {						
						console.log('Failed to update notifications count', status + ': ' + error);
					});					
				}, 10000);
			},
			onDestroy : function() {
				// stop notification timer
				clearInterval(this.timer);
				// close search form
				closeSearchForm(0);
				// remove all event handlers
				Lvl.vent.off('editable:items:dragstart');
				Lvl.vent.off('editable:items:dragend');
				$(document).off('keyup', this.handleEscKeyUpEvent);
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
								url : Lvl.config.get('service.url') + '/notifications/~',
								type : 'GET',
								headers : Lvl.config.authorizationHeader(),
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