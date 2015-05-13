/**
 * RequireJS module that defines the controller: header->admin.
 */

define([ 'app', 'apps/header/show/header_admin_view' ], function(Lvl, View) {
	Lvl.module('HeaderApp.Admin', function(Admin, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Admin.Controller = {
			showHeader : function() {
				var view = new View.Header();
				Lvl.headerRegion.show(view);
				return View.Header.id;
			}
		}
	});
	return Lvl.HeaderApp.Admin.Controller;
});