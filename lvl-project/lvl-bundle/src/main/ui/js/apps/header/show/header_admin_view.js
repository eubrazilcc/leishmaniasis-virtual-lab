/**
 * RequireJS module that defines the view: header->admin.
 */

define([ 'app', 'tpl!apps/header/show/tpls/header_admin', 'tpl!apps/header/show/tpls/header_nav', 'tpl!apps/header/show/tpls/header_nav_link',
		'tpl!apps/header/show/tpls/header_notifications', 'moment', 'qtip' ], function(Lvl, AdminHeaderTpl, NavigationTpl, NavigationLinkTpl, NotificationsTpl,
		moment) {
	Lvl.module('HeaderApp.Admin.View', function(View, Lvl, Backbone, Marionette, $, _) {
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
		View.id = 'admin';
		View.Header = Marionette.LayoutView.extend({
			template : AdminHeaderTpl,
			templateHelpers : function() {
				$.ajax({
					type : 'GET',
					url : Lvl.config.get('auth') + '/users/' + Lvl.config.session.get('user.session').email + "?use_email=true",
					headers : Lvl.config.authorizationHeader(),
					dataType : 'json'
				}).done(function(data, textStatus, request) {
					if (data.firstname) {
						$('#username').html(function(idx, oldhtml) {
							return oldhtml.replace(/Noname/, data.firstname);
						});
					}
					if (data.roles && $.isArray(data.roles) && _.contains(data.roles, 'admin')) {
						$('#sectionAdmin').removeClass('hidden');
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
			},
			onDestroy : function() {
				closeSearchForm(0);
				// remove all event handlers
				Lvl.vent.off('editable:items:dragstart');
				Lvl.vent.off('editable:items:dragend');
				$(document).off('keyup', this.handleEscKeyUpEvent);
			},			
			onRender : function(options) {
				this.$('#btnAlerts').click(function(event) {
					event.preventDefault();
				}).qtip({
					content : {
						text : function(event, api) {
							api.elements.content.html('<img src="/img/ajax_loader_gray_32.gif" alt="Loading..."/>');
							return $.ajax({
								url : Lvl.config.get('service', '') + '/notifications',
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
	return Lvl.HeaderApp.Admin.View;
});