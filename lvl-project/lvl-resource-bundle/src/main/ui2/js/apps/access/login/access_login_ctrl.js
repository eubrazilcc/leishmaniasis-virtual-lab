/**
 * RequireJS module that defines the controller: access->login.
 */

define([ 'app', 'apps/access/login/access_login_view' ], function(Lvl, View) {
	Lvl.module('AccessApp.Login', function(Login, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Login.Controller = {
			login : function(target, reason) {
				var view = new View.Content({
					model : new Backbone.Model({
						'target' : target,
						'reason' : reason
					})
				});
				Lvl.mainRegion.show(view);
			}
		}
	});
	return Lvl.AccessApp.Login.Controller;
});