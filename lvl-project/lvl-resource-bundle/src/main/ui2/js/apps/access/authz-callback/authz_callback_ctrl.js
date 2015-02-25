/**
 * RequireJS module that defines the controller: access->authorization callback.
 */

define([ 'app' ], function(Lvl) {
	Lvl.module('AccessApp.AuthzCallback', function(AuthzCallback, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		AuthzCallback.Controller = {
			authorize : function() {
				var provider = this.model.get('provider') || 'linkedin';
				var section = this.model.get('section') || 'home';

				// TODO : grab access token

				Lvl.navigate(section, {
					trigger : true,
					replace : true
				});
			}
		}
	});
	return Lvl.AccessApp.AuthzCallback.Controller;
});