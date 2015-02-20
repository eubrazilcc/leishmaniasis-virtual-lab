/**
 * RequireJS module that defines the controller: drive->datasets.
 */

define([ 'app', 'apps/config/marionette/configuration', 'entities/dataset', 'apps/drive/datasets/drive_datasets_view' ], function(Lvl, Configuration,
		DatasetModel, View) {
	Lvl.module('DriveApp.Datasets', function(Datasets, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		Datasets.Controller = {
			showSection : function() {
				var view = new View.Content({
					collection : new DatasetModel.DatasetPageableCollection({
						oauth2_token : config.authorizationToken()
					})
				});
				view.on('dataset:link:create', function(filename) {
					require([ 'entities/link', 'apps/drive/links/create_view' ], function(LinkModel, EditView) {
						var links = new LinkModel.LinkAllCollection({
							oauth2_token : config.authorizationToken()
						});
						var dialogView = new EditView.Content({
							collection : links,
							'filename' : filename
						});						
						Lvl.dialogRegion.show(dialogView);
					});
				});				
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.DriveApp.Datasets.Controller;
});