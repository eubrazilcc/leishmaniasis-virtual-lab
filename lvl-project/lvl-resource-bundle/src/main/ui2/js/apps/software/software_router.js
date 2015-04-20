/**
 * RequireJS module that defines the routes of the sub-application: software.
 */

define([ 'app', 'routefilter' ], function(Lvl) {
	Lvl.module('Routers.SoftwareApp', function(SoftwareAppRouter, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var Router = Backbone.Router.extend({
			routes : {
				'software' : 'showSoftware',
				'software/:section' : 'showSoftware'
			},
			before : function() {
				require([ 'apps/software/software_app' ], function() {
					Lvl.execute('set:active:header', 'home');
					Lvl.execute('set:active:footer', 'home');
					Lvl.startSubApp('SoftwareApp');
				});
			},
			showSoftware : function(section) {
				section = (section || 'software').toLowerCase();
				if (section === 'software' || section === 'releases' || section === 'downloads' || section === 'development') {
					Lvl.execute('show:software', section);
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