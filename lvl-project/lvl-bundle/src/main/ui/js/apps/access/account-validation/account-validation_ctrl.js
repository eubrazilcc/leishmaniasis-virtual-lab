/**
 * RequireJS module that defines the controller: access->account-validation.
 */

define([ 'app', 'apps/access/account-validation/account-validation_view' ], function(Lvl, View) {
	Lvl.module('AccessApp.AccountValidation', function(AccountValidation, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		AccountValidation.Controller = {
			validate : function(email, code) {
				var view = new View.Content({
					model : new Backbone.Model({
						'email' : email,
						'code' : code
					})
				});
				Lvl.mainRegion.show(view);
			}
		}
	});
	return Lvl.AccessApp.AccountValidation.Controller;
});