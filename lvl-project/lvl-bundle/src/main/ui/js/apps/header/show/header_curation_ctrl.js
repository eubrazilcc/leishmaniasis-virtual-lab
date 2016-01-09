/**
 * RequireJS module that defines the controller: header->curation.
 */

define([ 'app', 'apps/header/show/header_curation_view' ], function(Lvl, View) {
	Lvl.module('HeaderApp.Curation', function(Curation, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Curation.Controller = {
			showHeader : function() {
				var view = new View.Header();
				Lvl.headerRegion.show(view);
				return View.Header.id;
			}
		}
	});
	return Lvl.HeaderApp.Curation.Controller;
});