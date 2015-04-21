/**
 * RequireJS module that defines the sub-application: documentation.
 */

define([ 'app' ], function(Lvl) {
	Lvl.module('DocumentationApp', function(DocumentationApp, Lvl, Backbone, Marionette, $, _) {
		'use strict';

		/* Initialization & finalization */
		DocumentationApp.startWithParent = false;

		DocumentationApp.onStart = function() {
			console.log('starting DocumentationApp');
		};

		DocumentationApp.onStop = function() {
			console.log('stopping DocumentationApp');
		};

		/* Commands and events */
		Lvl.commands.setHandler('show:documentation', function(section) {
			require([ 'apps/documentation/show/documentation_show_ctrl' ], function(ShowController) {
				ShowController.showDocumentation(section);
			});
		});
	});
});