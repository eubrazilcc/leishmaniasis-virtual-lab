/**
 * RequireJS module that defines the view: header->home.
 */

define([ 'app', 'tpl!apps/header/show/tpls/header_home', 'qtip' ], function(Lvl, HomeHeaderTpl) {
	Lvl.module('HeaderApp.Home.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		View.id = 'home';
		View.Header = Marionette.ItemView.extend({
			template : HomeHeaderTpl,
			templateHelpers : {
				sessionLink : function() {
					return Lvl.config.isAuthenticated() ? 'logout' : 'login';
				},
				sessionText : function() {
					return Lvl.config.isAuthenticated() ? '<i class="fa fa-sign-out"></i> Sign out' : '<i class="fa fa-sign-in"></i> Sign in';
				}
			}
		});
	});
	return Lvl.HeaderApp.Home.View;
});