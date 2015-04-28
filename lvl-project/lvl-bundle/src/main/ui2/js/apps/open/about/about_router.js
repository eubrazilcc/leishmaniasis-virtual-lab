/**
 * RequireJS module that defines the routes of the sub-application: open->about.
 */

define([ 'app', 'routefilter' ], function(Lvl) {
	Lvl.module('Routers.AboutApp', function(AboutAppRouter, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var Router = Backbone.Router.extend({
			routes : {
				'about' : 'showAbout',
				'about/:section' : 'showAbout'
			},
			before : function() {
				require([ 'apps/open/about/about_app' ], function() {
					Lvl.execute('set:active:header', 'home');
					Lvl.execute('set:active:footer', 'home');
					Lvl.startSubApp('AboutApp');
				});
			},
			showAbout : function(section) {
				section = (section || 'about').toLowerCase();
				if (section === 'about' || section === 'project' || section === 'key-features') {
					Lvl.execute('show:about', section);
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