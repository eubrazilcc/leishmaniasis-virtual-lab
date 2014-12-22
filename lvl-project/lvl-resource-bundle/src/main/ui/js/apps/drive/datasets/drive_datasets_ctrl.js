/**
 * RequireJS module that defines the controller: drive->datasets.
 */

define([ 'app', 'apps/config/marionette/configuration', 'entities/link', 'apps/drive/datasets/drive_datasets_view' ], function(Lvl, Configuration, 
		LinkModel, View) {
	Lvl.module('DriveApp.Datasets', function(Datasets, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		Datasets.Controller = {
				showSection : function() {
					var view = new View.Content({
						collection : new LinkModel.LinkPageableCollection({
							oauth2_token : config.authorizationToken()
						})
					});
					Lvl.mainRegion.currentView.tabContent.show(view);
					return View.Content.id;
				}
		}
	});
	return Lvl.DriveApp.Datasets.Controller;
});