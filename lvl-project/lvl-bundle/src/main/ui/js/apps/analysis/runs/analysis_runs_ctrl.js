/**
 * RequireJS module that defines the controller: analysis->runs.
 */

define([ 'app', 'entities/workflow_run', 'apps/analysis/runs/analysis_runs_view' ], function(Lvl, WorkflowRunModel, View) {
	Lvl.module('AnalysisApp.Runs', function(Runs, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Runs.Controller = {
			showSection : function() {
				var view = new View.Content({
					collection : new WorkflowRunModel.WorkflowRunPageableCollection({
						oauth2_token : Lvl.config.authorizationToken()
					})
				});
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.AnalysisApp.Runs.Controller;
});