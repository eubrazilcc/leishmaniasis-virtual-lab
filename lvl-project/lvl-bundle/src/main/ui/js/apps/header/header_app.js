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
		
		var CurrentCollection = Backbone.Model.extend({ });
		
		HeaderApp.currentCollection = new CurrentCollection;

		HeaderApp.currentHeader = null;

		/**
		 * Sets the active header. The 'id' parameter defines the header type:
		 * home, workspace, admin or no_header (default option). The
		 * 'application' parameter defines the current application: DNA sequence
		 * collection, social network, e-compendium, etc.
		 */
		Lvl.commands.setHandler('set:active:header', function(id, application, section, collectionName) {
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
						HeaderApp.currentHeader = WorkspaceHeaderCtrl.showHeader(HeaderApp.navLinks, HeaderApp.currentCollection);
					});
				} else if (id === 'admin') {
					require([ 'apps/header/show/header_admin_ctrl' ], function(AdminHeaderCtrl) {
						HeaderApp.currentHeader = AdminHeaderCtrl.showHeader();
					});
				} else if (id === 'curation') {
					require([ 'apps/header/show/header_curation_ctrl' ], function(CurationHeaderCtrl) {
						HeaderApp.currentHeader = CurationHeaderCtrl.showHeader();
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
			// set the current collection
			if (application === 'collection') {
				HeaderApp.currentCollection.set({ section: section, name: collectionName });
			}
		});
	});
	return Lvl.HeaderApp;
});