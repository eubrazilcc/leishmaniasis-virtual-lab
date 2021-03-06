/**
 * RequireJS module that defines the controller: header->home.
 */

define([ 'app', 'apps/header/show/header_home_view' ], function(Lvl, View) {
	Lvl.module('HeaderApp.Home', function(Home, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Home.Controller = {
			showHeader : function() {
				var view = new View.Header();
				Lvl.headerRegion.show(view);
				return View.Header.id;
			}
		}
	});
	return Lvl.HeaderApp.Home.Controller;
});