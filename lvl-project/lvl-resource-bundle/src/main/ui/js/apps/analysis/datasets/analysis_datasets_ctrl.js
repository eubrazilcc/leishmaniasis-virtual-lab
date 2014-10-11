/**
 * RequireJS module that defines the controller: analysis->browse.
 */

define([ 'app', 'apps/config/marionette/configuration', 'entities/workflow_data', 'apps/analysis/datasets/analysis_datasets_view' ], function(Lvl, Configuration,
		WorkflowDataModel, View) {
	Lvl.module('AnalysisApp.Datasets', function(Datasets, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		Datasets.Controller = {
			showSection : function() {
				var view = new View.Content({
					collection : new WorkflowDataModel.WorkflowDataPageableCollection({
						oauth2_token : config.authorizationToken()
					})
				});
				view.on('analysis:dataset:add', function(datasets) {
					require([ 'entities/link', 'apps/analysis/upload/analysis_upload_dataset_view' ], function(LinkModel, UploadView) {
						var links = new LinkModel.LinkAllCollection({
							oauth2_token : config.authorizationToken()
						});
						var dialogView = new UploadView.Content({
							collection : links,
							'datasets' : datasets
						});
						links.fetch({
							reset : true
						}).done(function() {
							Lvl.dialogRegion.show(dialogView);
						});
					});
				});				
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.AnalysisApp.Datasets.Controller;
});