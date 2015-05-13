/**
 * RequireJS module that defines the routes of the sub-application: documentation.
 */

define([ 'app', 'routefilter' ], function(Lvl) {
	Lvl.module('Routers.DocumentationApp', function(DocumentationAppRouter, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var Router = Backbone.Router.extend({
			routes : {
				'documentation' : 'showDocumentation',
				'documentation/:section' : 'showDocumentation',
				'doc' : 'showDocumentation',
				'doc/:section' : 'showDocumentation'
			},
			before : function() {
				require([ 'apps/open/documentation/documentation_app' ], function() {
					Lvl.execute('set:active:header', 'home');
					Lvl.execute('set:active:footer', 'home');
					Lvl.startSubApp('DocumentationApp');
				});
			},
			showDocumentation : function(section) {
				section = (section || 'doc').toLowerCase();
				if (section === 'doc' || section === 'screencasts' || section === 'presentations' || section === 'publications') {
					Lvl.execute('show:documentation', section);
				} else if (section === 'documentation') {
					Lvl.execute('show:documentation', 'doc');
				} else {
					Lvl.navigate('not-found', {
						trigger : true,
						replace : true
					});
				}
			}
		});
		Lvl.addInitializer(function() {
			var router = new Router();
		});
	});
});