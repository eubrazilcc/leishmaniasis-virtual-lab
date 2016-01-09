/**
 * RequireJS module that defines the controller: social->posts.
 */

define([ 'app', 'entities/post', 'apps/social/posts/social_posts_view' ], function(Lvl, PostModel, View) {
	Lvl.module('SocialApp.Posts', function(Posts, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Posts.Controller = {
			showSection : function() {
				var view = new View.Content({
					collection : new PostModel.PostPageableCollection({
						oauth2_token : Lvl.config.authorizationToken()
					})
				});				
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.SocialApp.Posts.Controller;
});