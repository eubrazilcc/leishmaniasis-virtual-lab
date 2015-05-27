/**
 * RequireJS module that defines the controller: analysis->pipelines.
 */

define([ 'app', 'entities/workflow', 'apps/analysis/pipelines/analysis_pipelines_view' ], function(Lvl, WorkflowModel, View) {
	Lvl.module('AnalysisApp.Pipelines', function(Pipelines, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Pipelines.Controller = {
			showSection : function() {
				var view = new View.Content({
					collection : new WorkflowModel.WorkflowDefinitionPageableCollection({
						oauth2_token : Lvl.config.authorizationToken()
					})
				});
				view.on('analysis:pipeline:run', function(wfId, wfConf) {					
					require([ 'apps/analysis/submit/analysis_submit_pipeline_view' ], function(SubmitView) {
						var dialogView = new SubmitView.Content({
							'workflowId' : wfId,
							'wfConf' : wfConf
						});
						Lvl.dialogRegion.show(dialogView);
					});
				});
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.AnalysisApp.Pipelines.Controller;
});