/**
 * RequireJS module that defines the sub-application: header.
 */

define([ 'app', 'entities/navigation' ], function(Lvl) {
	Lvl.module('HeaderApp', function(HeaderApp, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		HeaderApp.startWithParent = true;

		HeaderApp.onStart = function() {
			console.log('starting HeaderApp');
		};

		HeaderApp.onStop = function() {
			console.log('stopping HeaderApp');
		};

		HeaderApp.navLinks = Lvl.request('navigation:links+settings:entities');

		HeaderApp.currentHeader = null;

		/**
		 * Sets the active header. The 'id' parameter defines the header type:
		 * home, workspace or no_header (default option). The 'application'
		 * parameter defines the current application: DNA sequence collection,
		 * social network, e-compendium, etc.
		 */
		Lvl.commands.setHandler('set:active:header', function(id, application) {
			id = (id || 'default').toLowerCase();
			application = (application || 'home').toLowerCase();
			// load header based on the id
			if (HeaderApp.currentHeader !== id) {
				if (id === 'home') {
					require([ 'apps/header/show/header_home_ctrl' ], function(HomeHeaderCtrl) {
						HeaderApp.currentHeader = HomeHeaderCtrl.showHeader();
					});
				} else if (id === 'workspace') {
					require([ 'apps/header/show/header_workspace_ctrl' ], function(WorkspaceHeaderCtrl) {
						HeaderApp.currentHeader = WorkspaceHeaderCtrl.showHeader(HeaderApp.navLinks);
					});
				} else {
					Lvl.headerRegion.reset();
					HeaderApp.currentHeader = null;
				}
			}
			// select a link based on the application
			if (id === 'workspace' && (HeaderApp.navLinks.selected === undefined || HeaderApp.navLinks.selected.get('href') !== '/#' + application)) {
				var navLinkToSelect = HeaderApp.navLinks.find(function(link) {
					return link.get('href') === '/#' + application;
				});
				navLinkToSelect.select();
				HeaderApp.navLinks.trigger('reset');
			}
		});
	});
	return Lvl.HeaderApp;
});