/**
 * RequireJS module that defines the routes of the sub-application: enm.
 */

define([ 'app', 'routefilter' ], function(Lvl) {
	Lvl.module('Routers.EnmApp', function(EnmAppRouter, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var Router = Backbone.Router.extend({
			routes : {
				'enm' : 'showEnm'
			},
			before : function() {
				if (!Lvl.config.isAuthenticated()) {
					Lvl.navigate('login/' + encodeURIComponent(Backbone.history.fragment) + '/unauthenticated', {
						trigger : true,
						replace : true
					});
					return false;
				}
				require([ 'apps/enm/enm_app' ], function() {
					Lvl.execute('set:active:header', 'workspace', 'enm');
					Lvl.execute('set:active:footer', 'workspace');
					Lvl.startSubApp('EnmApp');
				});
				return true;
			},
			showEnm : function() {
				Lvl.execute('show:enm');
			}
		});

		Lvl.addInitializer(function() {
			var router = new Router();
		});
	});
});