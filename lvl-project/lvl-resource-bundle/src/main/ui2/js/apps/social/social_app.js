/**
 * RequireJS module that defines the sub-application: social.
 */

define([ 'app' ], function(Lvl) {
	Lvl.module('SocialApp', function(SocialApp, Lvl, Backbone, Marionette, $, _) {
		'use strict';

		/* Initialization & finalization */
		SocialApp.startWithParent = false;

		SocialApp.onStart = function() {
			console.log('starting SocialApp');
		};

		SocialApp.onStop = function() {
			console.log('stopping SocialApp');
		};

		/* Commands and events */
		Lvl.commands.setHandler('show:social', function() {
			require([ 'apps/social/show/social_show_ctrl' ], function(ShowController) {
				ShowController.showSocial();
			});
		});
	});
});