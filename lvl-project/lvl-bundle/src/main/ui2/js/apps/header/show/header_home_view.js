/**
 * RequireJS module that defines the view: header->home.
 */

define([ 'app', 'tpl!apps/header/show/templates/header_home', 'apps/config/marionette/configuration' ], function(Lvl, HomeHeaderTpl, Configuration) {
	var config = new Configuration();
	Lvl.module('HeaderApp.Home.View', function(View, Lvl, Backbone, Marionette, $, _) {
		View.id = 'home';
		View.Header = Marionette.ItemView.extend({
			template : HomeHeaderTpl,
			templateHelpers : {
				sessionLink : function() {
					return config.isAuthenticated() ? 'logout' : 'login';
				},
				sessionText : function() {
					return config.isAuthenticated() ? '<i class="fa fa-sign-out"></i> Sign out' : '<i class="fa fa-sign-in"></i> Sign in';
				}
			}
		});
	});
	return Lvl.HeaderApp.Home.View;
});