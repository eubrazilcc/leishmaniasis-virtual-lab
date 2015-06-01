/**
 * RequireJS module that defines the entity: navigation link.
 */

define([ 'app', 'backbone.picky' ], function(Lvl) {
	Lvl.module('Entities.Navigation', function(Entities, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Entities.Navigation = Backbone.Model.extend({
			defaults : {
				href : '/#home',
				icon : 'fa-chain-broken',
				text : 'Unknown',
				isExternal : undefined /* labels external links that should be opened in a new tab/window */
			},
			initialize : function() {
				var selectable = new Backbone.Picky.Selectable(this);
				_.extend(this, selectable);
			},
			validate : function(attrs, options) {
				var errors = {};
				if (!attrs.href) {
					errors.href = 'can\'t be empty';
				}
				if (!attrs.icon) {
					errors.icon = 'can\'t be empty';
				}
				if (!attrs.text) {
					errors.text = 'can\'t be empty';
				} else {
					if (attrs.text.length < 2) {
						errors.text = 'is too short';
					}
				}
				if (!_.isEmpty(errors)) {
					return errors;
				}
			}
		});

		Entities.NavigationCollection = Backbone.Collection.extend({
			model : Entities.Navigation,
			comparator : 'id',
			initialize : function() {
				var singleSelect = new Backbone.Picky.SingleSelect(this);
				_.extend(this, singleSelect);
			}
		});

		var iniNavigationLinks = function() {
			Entities.navigationLinks = new Entities.NavigationCollection([ {
				id : 1,
				href : '/#home',
				icon : 'fa-home',
				text : 'Home'
			}, {
				id : 2,
				href : '/#social',
				icon : 'fa-comments',
				text : 'Community'
			}, {
				id : 3,
				href : '/#collection',
				icon : 'fa-database',
				text : 'Collection'
			}, {
				id : 4,
				href : '/#e-compendium',
				icon : 'fa-file-text',
				text : 'e-Compendium'
			}, {
				id : 5,
				href : '/#analysis',
				icon : 'fa-barcode',
				text : 'Molecular Analysis'
			}, {
				id : 6,
				href : '/#enm',
				icon : 'fa-globe',
				text : 'Ecological Niche Modelling'
			}, {
				id : 7,
				href : '/#drive',
				icon : 'fa-hdd-o',
				text : 'Drive'
			} ]);
		};

		var iniSettingsLinks = function() {
			Entities.settingsLinks = new Entities.NavigationCollection([ {
				id : 9,
				href : '/#saved-items',
				icon : 'fa-archive',
				text : 'Saved Items'
			}, {
				id : 10,
				href : '/#settings',
				icon : 'fa-cog',
				text : 'Settings'
			} ]);
		};
		
		var iniAdminLinks = function() {
			Entities.adminLinks = new Entities.NavigationCollection([ {
				id : 12,
				href : '/#admin',
				icon : 'fa-wrench',
				text : 'Admin'
			} ]);
		};

		var iniAboutLinks = function() {
			Entities.aboutLinks = new Entities.NavigationCollection([ {
				id : 14,
				href : '/#about/project',
				icon : 'fa-group',
				text : 'Project'
			}, {
				id : 15,
				href : '/#about/key-features',
				icon : 'fa-key',
				text : 'Key Features'
			} ]);
		};

		var iniDocumentationLinks = function() {
			Entities.documentationLinks = new Entities.NavigationCollection([ {
				id : 17,
				href : '/#doc/screencasts',
				icon : 'fa-video-camera',
				text : 'Screencasts'
			}, {
				id : 18,
				href : '/#doc/presentations',
				icon : 'fa-desktop',
				text : 'Presentations'
			}, {
				id : 19,
				href : '/#doc/publications',
				icon : 'fa-file-text-o',
				text : 'Publications'
			} ]);
		};

		var iniSupportLinks = function() {
			Entities.supportLinks = new Entities.NavigationCollection([ {
				id : 21,
				href : '/#support/mailing-list',
				icon : 'fa-envelope-o',
				text : 'Mailing list'
			} ]);
		};

		var iniSoftwareLinks = function() {
			Entities.softwareLinks = new Entities.NavigationCollection([ {
				id : 23,
				href : '/#software/releases',
				icon : 'fa-bullhorn',
				text : 'Releases'
			}, {
				id : 24,
				href : '/#software/downloads',
				icon : 'fa-download',
				text : 'Downloads'
			}, {
				id : 25,
				href : '/#software/development',
				icon : 'fa-github-alt',
				text : 'Development'
			}, {
				id : 26,
				href : '/apidoc/',
				icon : 'fa-book',
				text : 'API Documentation',
				isExternal : true
			} ]);
		};

		var API = {
			getNavigationEntities : function() {
				if (Entities.navigationLinks === undefined) {
					iniNavigationLinks();
				}
				return Entities.navigationLinks;
			},
			getSettingEntities : function() {
				if (Entities.settingsLinks === undefined) {
					iniSettingsLinks();
				}
				return Entities.settingsLinks;
			},
			getAboutEntities : function() {
				if (Entities.aboutLinks === undefined) {
					iniAboutLinks();
				}
				return Entities.aboutLinks;
			},
			getDocumentationEntities : function() {
				if (Entities.documentationLinks === undefined) {
					iniDocumentationLinks();
				}
				return Entities.documentationLinks;
			},
			getSupportEntities : function() {
				if (Entities.supportLinks === undefined) {
					iniSupportLinks();
				}
				return Entities.supportLinks;
			},
			getSoftwareEntities : function() {
				if (Entities.softwareLinks === undefined) {
					iniSoftwareLinks();
				}
				return Entities.softwareLinks;
			},
			getNavigationSettingEntities : function() {
				if (Entities.navigationLinks === undefined) {
					iniNavigationLinks();
				}
				if (Entities.settingsLinks === undefined) {
					iniSettingsLinks();
				}
				var navigationLinksJson = Entities.navigationLinks.toJSON();
				return new Entities.NavigationCollection(navigationLinksJson.concat({
					'id' : _.max(navigationLinksJson, function(link) {
						return link.id;
					}).id + 1,
					'href' : '#',
					'icon' : 'fa-minus',
					'text' : 'separator'
				}).concat(Entities.settingsLinks.toJSON()));
			}
		}

		Lvl.reqres.setHandler('navigation:links:entities', function() {
			return API.getNavigationEntities();
		});

		Lvl.reqres.setHandler('navigation:settings:entities', function() {
			return API.getSettingEntities();
		});

		Lvl.reqres.setHandler('navigation:about:entities', function() {
			return API.getAboutEntities();
		});

		Lvl.reqres.setHandler('navigation:documentation:entities', function() {
			return API.getDocumentationEntities();
		});

		Lvl.reqres.setHandler('navigation:support:entities', function() {
			return API.getSupportEntities();
		});

		Lvl.reqres.setHandler('navigation:software:entities', function() {
			return API.getSoftwareEntities();
		});

		Lvl.reqres.setHandler('navigation:links+settings:entities', function() {
			return API.getNavigationSettingEntities();
		});
	});

	return Lvl.Entities.Navigation;
});