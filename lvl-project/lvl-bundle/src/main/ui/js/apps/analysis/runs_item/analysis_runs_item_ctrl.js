/**
 * RequireJS module that defines the controller: analysis->runs_item.
 */

define([ 'app', 'entities/workflow_run', 'apps/analysis/runs_item/analysis_runs_item_view' ], function(Lvl, WorkflowRunModel, View) {
	Lvl.module('AnalysisApp.RunsItem', function(RunsItem, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		RunsItem.Controller = {
			showSection : function(itemId) {
				var runModel = new WorkflowRunModel.WorkflowRun();
				runModel.set('id', itemId);
				runModel.oauth2_token = Lvl.config.authorizationToken();
				var view = new View.Content({
					model : runModel
				});
				view.on('analysis:pipeline:product:show', function(productJSON) {
					var product = JSON.parse(productJSON);
					if (product.path.substr(product.path.length - '.nwk'.length, product.path.length) === '.nwk') {
						// TODO require([ 'apps/analysis/tree_viewer/analysis_tree_viewer_view' ], function(TreeViewerView) {
							// TODO var dialogView = new TreeViewerView.Content({
						require([ 'apps/analysis/text_viewer/analysis_text_viewer_view' ], function(TextViewerView) {	
							var dialogView = new TextViewerView.Content({
								'product' : product
							});
							Lvl.dialogRegion.show(dialogView);
						});
					} else {
						require([ 'apps/analysis/text_viewer/analysis_text_viewer_view' ], function(TextViewerView) {
							var dialogView = new TextViewerView.Content({
								'product' : product
							});
							Lvl.dialogRegion.show(dialogView);
						});
					}
				});
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.AnalysisApp.RunsItem.Controller;
});