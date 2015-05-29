/**
 * RequireJS module that defines the controller: analysis->runs_item_tree_viewer.
 */

define([ 'app', 'entities/workflow_run', 'apps/analysis/trevi/treviv' ], function(Lvl, WfRunModel, View) {
	Lvl.module('AnalysisApp.RunsItemTree', function(RunsItemTree, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		RunsItemTree.Controller = {
			showSection : function(itemId) {
				var runModel = new WfRunModel.WorkflowRun();
				runModel.set('id', itemId);
				runModel.oauth2_token = Lvl.config.authorizationToken();
				var view = new View.Content({
					model : runModel
				});				
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.AnalysisApp.RunsItemTree.Controller;
});