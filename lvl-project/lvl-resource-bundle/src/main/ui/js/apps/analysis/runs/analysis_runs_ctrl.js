/**
 * RequireJS module that defines the controller: analysis->runs.
 */

define([ 'app', 'apps/config/marionette/configuration', 'entities/workflow_run', 'apps/analysis/runs/analysis_runs_view' ], function(Lvl, Configuration,
		WorkflowRunModel, View) {
	Lvl.module('AnalysisApp.Runs', function(Runs, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		Runs.Controller = {
			showSection : function() {
				var view = new View.Content({
					collection : new WorkflowRunModel.WorkflowRunPageableCollection({
						oauth2_token : config.authorizationToken()
					})
				});
				view.on('analysis:pipeline:monitor', function(workflowRun) {
					require([ 'apps/analysis/monitor/analysis_monitor_invocation_view' ], function(MonitorView) {
						workflowRun.oauth2_token = config.authorizationToken();
						var dialogView = new MonitorView.Content({
							model : workflowRun
						});
						Lvl.dialogRegion.show(dialogView);
					});
				});
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.AnalysisApp.Runs.Controller;
});