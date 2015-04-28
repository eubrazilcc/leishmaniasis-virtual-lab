/**
 * RequireJS module that defines the controller: collection->submit.
 */

define([ 'app', 'apps/collection/submit/collection_submit_view' ], function(Lvl, View) {
	Lvl.module('CollectionApp.Submit', function(Submit, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Submit.Controller = {
			showSection : function() {
				var view = new View.Content();
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.CollectionApp.Submit.Controller;
});