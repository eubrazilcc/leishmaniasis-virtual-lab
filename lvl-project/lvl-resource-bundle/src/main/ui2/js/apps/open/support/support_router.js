/**
 * RequireJS module that defines the routes of the sub-application: open->support.
 */

define([ 'app', 'routefilter' ], function(Lvl) {
	Lvl.module('Routers.SupportApp', function(SupportAppRouter, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var Router = Backbone.Router.extend({
			routes : {
				'support' : 'showSupport',
				'support/:section' : 'showSupport'
			},
			before : function() {
				require([ 'apps/open/support/support_app' ], function() {
					Lvl.execute('set:active:header', 'home');
					Lvl.execute('set:active:footer', 'home');
					Lvl.startSubApp('SupportApp');
				});
			},
			showSupport : function(section) {
				section = (section || 'support').toLowerCase();
				if (section === 'support' || section === 'mailing-list') {
					Lvl.execute('show:support', section);
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