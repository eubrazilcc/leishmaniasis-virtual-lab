/**
 * RequireJS module that defines the controller: access->authorization callback.
 */

define([ 'app', 'apps/config/marionette/configuration' ], function(Lvl, Configuration) {
	Lvl.module('AccessApp.AuthzCallback', function(AuthzCallback, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		AuthzCallback.Controller = {
			authorize : function(provider, section, email, access_token) {
				var provider2 = provider || 'linkedin';
				var section2 = section || 'home';				
				if (email && access_token) {
					var config = new Configuration();
					config.saveSession(decodeURIComponent(email), decodeURIComponent(access_token), provider2, false);					
					Lvl.navigate(decodeURIComponent(section2), {
						trigger : true,
						replace : true
					});
				} else {
					Lvl.navigate('login/' + target + '/refused', {
						trigger : true,
						replace : true
					});
				}
			}
		}
	});
	return Lvl.AccessApp.AuthzCallback.Controller;
});