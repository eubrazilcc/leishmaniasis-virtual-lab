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
					return config.isAuthenticated() ? '<span class="text-danger"><i class="fa fa-sign-out"></i></span> Sign out'
							: '<span class="text-success"><i class="fa fa-sign-in"></i></span> Sign in';
				}
			}
		});
	});
	return Lvl.HeaderApp.Home.View;
});