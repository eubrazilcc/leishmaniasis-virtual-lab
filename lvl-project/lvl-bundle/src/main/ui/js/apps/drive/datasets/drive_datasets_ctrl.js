/**
 * RequireJS module that defines the controller: drive->datasets.
 */

define([ 'app', 'entities/dataset', 'apps/drive/datasets/drive_datasets_view' ], function(Lvl, DatasetModel, View) {
	Lvl.module('DriveApp.Datasets', function(Datasets, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Datasets.Controller = {
			showSection : function() {
				var view = new View.Content({
					collection : new DatasetModel.DatasetPageableCollection({
						oauth2_token : Lvl.config.authorizationToken()
					})
				});
				view.on('datasets:file:upload', function() {
					require([ 'apps/drive/upload/ds_up' ], function(UploadView) {
						var dialogView = new UploadView.Content({});
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