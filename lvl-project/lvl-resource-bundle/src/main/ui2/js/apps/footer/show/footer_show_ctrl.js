/**
 * RequireJS module that defines the controller: footer->show.
 */

define([ 'app', 'apps/footer/show/footer_show_view' ], function(Lvl, View) {
	Lvl.module('FooterApp.Show', function(Show, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Show.Controller = {
			showFooter : function() {
				var view = new View.Footer();
				Lvl.footerRegion.show(view);
				return View.Footer.id;
			}
		}
	});
	return Lvl.FooterApp.Show.Controller;
});