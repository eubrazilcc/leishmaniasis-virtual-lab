/**
 * RequireJS module that defines the controller: drive->granted.
 */

define([ 'app', 'entities/obj_granted', 'apps/drive/granted/drive_granted_view' ], function(Lvl, ObjGrantedModel, View) {
	Lvl.module('DriveApp.Granted', function(Granted, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Granted.Controller = {
			showSection : function() {
				var view = new View.Content({
					collection : new ObjGrantedModel.ObjectGrantedPageableCollection({
						oauth2_token : Lvl.config.authorizationToken()
					})
				});
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.DriveApp.Granted.Controller;
});