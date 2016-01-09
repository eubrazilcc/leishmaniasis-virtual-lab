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
		Lvl.commands.setHandler('social:set:active', function(section) {
			require([ 'apps/social/layout/social_layout_ctrl' ], function(LayoutController) {
				SocialApp.currentSection = LayoutController.showLayout(section);
			});
		});
	});
});