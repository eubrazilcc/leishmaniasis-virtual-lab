/**
 * RequireJS module that defines the view: social->posts.
 */

define([ 'app', 'marionette', 'tpl!apps/social/posts/tpls/social_posts', 'tpl!apps/social/posts/tpls/post', 'tpl!apps/social/posts/tpls/view_new_posts', 'tpl!apps/social/posts/tpls/toolbar_browse', 'pace', 'moment', 'backbone.oauth2', 'bootstrapvalidator', 'backbone.syphon' ], 
		function(Lvl, Marionette, PostsTpl, PostTpl, ViewNewPostsTpl, ToolbarTpl, pace, moment) {
	Lvl.module('SocialApp.Posts.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';		
		View.Content = Marionette.ItemView.extend({
			id : 'posts',
			template : PostsTpl,
			initialize : function() {
				this.listenTo(this.collection, 'request', this.displaySpinner);
				this.listenTo(this.collection, 'sync error', this.removeSpinner);				
				// setup menu
				$('#lvl-floating-menu-toggle').show(0);
				$('#lvl-floating-menu').hide(0);
				$('#lvl-floating-menu').empty();
				$('#lvl-floating-menu').append(ToolbarTpl({}));				
				$('button#lvl-feature-tour-btn').on('click', this.startTour);
				// setup attributes
				this.lastUpdate = 0;
				// add notification timer
				var self = this;
				this.timer = setInterval(function() {					
					$.ajax({
						url : Lvl.config.get('service.url') + '/community/posts/~/' + self.getLastUpdate() + '/after',
						type : 'GET',
						headers : Lvl.config.authorizationHeader(),
						dataType : 'json'
					}).then(function(content) {
						if (content) {
							var count = content.totalCount || 0;
							if (count > 0) {
								$('#new-posts-container').html(ViewNewPostsTpl({ count: count }));
								$('#new-posts-container').removeClass('hidden');
							} else {
								$('#new-posts-container').addClass('hidden');
							}
						}
					}, function(xhr, status, error) {						
						console.log('Failed to get new posts', status + ': ' + error);
					});
				}, 10000);
			},			
			displaySpinner : function() {
				pace.restart();
				$('#post-container').fadeTo('fast', 0.4);
			},
			removeSpinner : function() {
				pace.stop();
				var self = this;
				$('#post-container').fadeTo('fast', 1);
				$('html,body').animate({
					scrollTop : 0
				}, '500', 'swing', function() {
					// nothing to do
				});
			},
			events : {
				'focus #lvlNewPostForm textarea.form-control' : function(e) {
					var form = $('#lvlNewPostForm');
					if (Boolean(form.attr('data-pristine') === 'true')) {
						form.attr('data-pristine', 'false');
						form.on('init.form.bv', function(e, data) {
							data.bv.disableSubmitButtons(true);
						}).bootstrapValidator({
							submitButtons : 'button[type="submit"]',
							fields : {
								'inputPost' : {
									verbose : false,
									validators : {
										notEmpty : {
											message : 'The content is required and cannot be empty'
										},
										stringLength : {
											max : 500,
											message : 'The content must be less than 500 characters long'
										}
									}
								}														
							}
						}).on('success.field.bv', function(e, data) {
							var isValid = data.bv.isValid();
							data.bv.disableSubmitButtons(!isValid);
						});
					}
				},
				'click #lvlNewPostSubmitBtn' : function(e) {
					e.preventDefault();
					pace.restart();					
					$('#lvlNewPostForm button[type="submit"]').attr('disabled', 'disabled');
					var self = this, formData = Backbone.Syphon.serialize(this);
					require([ 'entities/post' ], function(PostModel) {						
						var newPost = new PostModel.PostCreate();
						newPost.oauth2_token = Lvl.config.authorizationToken();
						newPost.save({
							'category' : 'MESSAGE',
							'level' : 'NORMAL',
							'body' : formData.inputPost							
						}, {							
							success : function(model, resp, options) {
								require([ 'common/growl' ], function(createGrowl) {
									var anchor = $('<a>', {
										href : options.xhr.getResponseHeader('Location')
									})[0];
									var id = anchor.pathname.substring(anchor.pathname.lastIndexOf('/') + 1); // unused
									require([ 'common/growl' ], function(createGrowl) {				
										createGrowl('New post created', 'Your post will be available soon in the <a href="/#social/posts">community</a> section', false);
									});
								});
							},
							error : function(model, resp, options) {
								require([ 'common/alert' ], function(alertDialog) {
									alertDialog('Error', 'Failed to create post.');
								});
							}
						}).always(function() {
							pace.stop();
							var form = self.$('#lvlNewPostForm');
							form[0].reset();
							form.bootstrapValidator('resetForm', true);
							form.bootstrapValidator('disableSubmitButtons', true);
						});
					});					
				},
				'click #older-posts' : function(e) {
					e.preventDefault();
					this.updateCollection(false);
				},
				'click #view-new-posts' : function(e) {
					e.preventDefault();
					var self = this;
					self.updateCollection();
					$('#new-posts-container').addClass('hidden');
				}
			},
			startTour : function(e) {
				e.preventDefault();
				require([ 'apps/social/posts/tours/posts_tour' ], function(tour) {
					tour();
				});
			},
			onDestroy : function() {
				// stop notification timer
				clearInterval(this.timer);
				// stop all listeners
				pace.stop();
				this.stopListening();
				// remove all event handlers				
				$('button#lvl-feature-tour-btn').unbind();
				// clean menu
				$('#lvl-floating-menu').hide(0);
				$('#lvl-floating-menu-toggle').hide(0);
				$('#lvl-floating-menu').empty();
				// clean tour
				require([ 'hopscotch' ], function(hopscotch) {
					hopscotch.endTour();
				});
			},
			onRender : function() {
				pace.start();
				this.updateCollection();
			},
			getIcon : function(category) {
				var icon = '';
				switch (category) {
				case 'ANNOUNCEMENT':
					icon = 'bullhorn';
					break;
				case 'INCIDENCE':
					icon = 'exclamation-circle';
					break;
				case 'MESSAGE':
				default:
					icon = 'comment';
					break;
				}
				return icon;
			},
			getLastUpdate : function() {
				return this.lastUpdate || 0;
			},
			getOldestDisplayed : function() {
				var lastDisplayedPost = $('#posts-list #postContainer').last();				
				return lastDisplayedPost ? lastDisplayedPost.find('#timestamp').text() : 0;				
			},
			updateCollection : function(descending) {
				var self = this;
				descending = descending !== undefined && typeof(descending) === 'boolean' ? descending : true;
				self.collection.queryParams.q = 'created:' + (descending ? '>' + self.getLastUpdate() : '<' + self.getOldestDisplayed());				
				self.collection.fetch({
					reset : true,
					success : function(collection, resp, options) {
						if (resp && resp.elements && resp.elements.length > 0) {
							$('#no-post-found').addClass('hidden');
							if (descending) {
								resp.elements.sort(function(a, b) {
									return a.created - b.created;
								});
							} else {
								resp.elements.sort(function(a, b) {
									return b.created - a.created;
								});
							}							
							if (descending) self.lastUpdate = resp.elements[resp.elements.length - 1].created;
							_.each(resp.elements, function(item) {
								var newPost = PostTpl({
									icon : self.getIcon(item.category),
									authorId : item.author,
									postId : item.id,
									body : item.body,
									created : moment(item.created, 'x').fromNow(),
									timestamp : item.created
								});
								if (descending) $('#posts-list').prepend(newPost);
								else $('#posts-list').append(newPost);
							});
							var oldestDisplayed = self.getOldestDisplayed();
							if (oldestDisplayed !== Number.MAX_VALUE) {
								$.ajax({
									url : Lvl.config.get('service.url') + '/community/posts/~/' + oldestDisplayed + '/before',
									type : 'GET',
									headers : Lvl.config.authorizationHeader(),
									dataType : 'json'
								}).then(function(content) {
									if (content) {
										var count = content.totalCount || 0;
										if (count > 0) {
											$('#older-posts-container').removeClass('hidden');
										} else {										
											$('#older-posts-container').addClass('hidden');
										}
									}
								}, function(xhr, status, error) {						
									console.log('Failed to get older posts', status + ': ' + error);
								});
							}							
						} else {
							$('#no-post-found').removeClass('hidden');
						}
					},
					error : function(collection, response, options) {
						require([ 'common/alert' ], function(createAlert) {
							createAlert('Failed to get posts from server',
								'Retry later and <a href="/#support/report-an-issue">report an issue</a> if the problem persists');
						});
					}
				});
			}
		});
	});
	return Lvl.SocialApp.Posts.View;
});