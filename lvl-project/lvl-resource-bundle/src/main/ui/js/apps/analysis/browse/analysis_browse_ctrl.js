/**
 * RequireJS module that defines the controller: analysis->browse.
 */

define([ 'app', 'apps/config/marionette/configuration', 'entities/workflow', 'apps/analysis/browse/analysis_browse_view' ], function(Lvl, Configuration,
		WorkflowModel, View) {
	Lvl.module('AnalysisApp.Browse', function(Browse, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		Browse.Controller = {
			showSection : function() {
				var view = new View.Content({
					collection : new WorkflowModel.WorkflowDefinitionPageableCollection({
						oauth2_token : config.authorizationToken()
					})
				});
				view.on('analysis:workflow:run', function(workflowId) {
					require([ 'entities/link', 'apps/analysis/run/run_workflow_view' ], function(LinkModel, EditView) {
						var links = new LinkModel.LinkAllCollection({
							oauth2_token : config.authorizationToken()
						});
						var dialogView = new EditView.Content({
							collection : links,
							'workflowId' : workflowId
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
	return Lvl.AnalysisApp.Browse.Controller;
});