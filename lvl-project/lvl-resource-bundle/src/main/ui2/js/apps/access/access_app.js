/**
 * RequireJS module that defines the sub-application: access.
 */

define([ 'app' ], function(Lvl) {
	Lvl.module('AccessApp', function(AccessApp, Lvl, Backbone, Marionette, $, _) {
		'use strict';

		/* Initialization & finalization */
		AccessApp.startWithParent = false;

		AccessApp.onStart = function() {
			console.log('starting AccessApp');
		};

		AccessApp.onStop = function() {
			console.log('stopping AccessApp');
		};

		/* Commands and events */
		Lvl.commands.setHandler('show:login', function(target, reason) {
			require([ 'apps/access/login/access_login_ctrl' ], function(LoginController) {
				LoginController.login(target, reason);
			});
		});

		Lvl.commands.setHandler('show:registration', function() {
			require([ 'apps/access/register/access_register_ctrl' ], function(RegisterController) {
				RegisterController.register();
			});
		});

		Lvl.commands.setHandler('show:account:validation', function(email, code) {
			require([ 'apps/access/account-validation/account-validation_ctrl' ], function(AccountController) {
				AccountController.validate(email, code);
			});
		});
	});
});