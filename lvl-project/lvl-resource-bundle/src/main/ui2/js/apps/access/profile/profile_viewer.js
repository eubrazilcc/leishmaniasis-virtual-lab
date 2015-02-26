/**
 * RequireJS module that defines the view: access->profile->user profile viewer.
 */

define([ 'app', 'tpl!apps/access/profile/templates/profile' ], function(Lvl, UserProfileTpl) {
	Lvl.module('AccessApp.UserProfile.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		View.Content = Marionette.ItemView.extend({
			template : UserProfileTpl			
		});
	});
	return Lvl.AccessApp.UserProfile.View;
});