/**
 * RequireJS module that defines the view: access->profile->user profile viewer.
 */

define([ 'app', 'tpl!apps/access/profile/templates/profile' ], function(Lvl, UserProfileTpl) {
	Lvl.module('AccessApp.UserProfile.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		View.Content = Marionette.ItemView.extend({
			template : UserProfileTpl,
			templateHelpers : function() {
				return {
					username : this.model.get('userid'),
					provider : this.model.get('provider') || 'lvl',
					formattedname : this.model.get('firstname') + ' ' + this.model.get('lastname'),
					pictureUrl : this.model.get('pictureUrl'),
					roles : (this.model.get('roles') || [ 'user' ]).join(', ')
				}
			},
			initialize : function() {
				this.listenTo(this.model, 'change', this.render);
				var self = this;
				self.model.fetch({
					reset : true
				});
			}			
		});
	});
	return Lvl.AccessApp.UserProfile.View;
});