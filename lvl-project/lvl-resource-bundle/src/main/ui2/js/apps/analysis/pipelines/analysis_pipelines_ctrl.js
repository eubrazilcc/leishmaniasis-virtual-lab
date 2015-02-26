/**
 * RequireJS module that defines the controller: analysis->pipelines.
 */

define([ 'app', 'apps/config/marionette/configuration', 'entities/workflow', 'apps/analysis/pipelines/analysis_pipelines_view' ], function(Lvl, Configuration,
		WorkflowModel, View) {
	Lvl.module('AnalysisApp.Pipelines', function(Pipelines, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		Pipelines.Controller = {
			showSection : function() {
				var view = new View.Content({
					collection : new WorkflowModel.WorkflowDefinitionPageableCollection({
						oauth2_token : config.authorizationToken()
					})
				});
				view.on('analysis:pipeline:run', function(workflowId) {
					require([ 'entities/dataset', 'apps/analysis/submit/analysis_submit_pipeline_view' ], function(DatasetModel, SubmitView) {
						var datasets = new DatasetModel.DatasetAllCollection({
							oauth2_token : config.authorizationToken()
						});
						var dialogView = new SubmitView.Content({
							collection : datasets,
							'workflowId' : workflowId
						});
						datasets.fetch({
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
	return Lvl.AnalysisApp.Pipelines.Controller;
});