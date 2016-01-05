/**
 * RequireJS module that defines the controller: collection->samples.
 */

define([ 'app', 'entities/sample', 'apps/collection/samples/collection_samples_view' ], function(Lvl, SampleModel, View) {
	Lvl.module('CollectionApp.Samples', function(Samples, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Samples.Controller = {
			showSection : function(id) {
				var collectionId = id || 'sandflies';
				var view = new View.Content({
					collection : new SampleModel.SamplePageableCollection({
						oauth2_token : Lvl.config.authorizationToken(),
						data_source : collectionId
					})
				});
				view.on('samples:view:sample', function(collectionId, sampleId) {
					require([ 'apps/collection/sample_viewer/collection_sample_viewer', 'entities/sample' ], function(SampleView, SampleModel) {
						var sampleModel = new SampleModel.Sample({
							'dataSource' : collectionId,
							'id' : sampleId
						});
						sampleModel.oauth2_token = Lvl.config.authorizationToken();
						var dialogView = new SampleView.Content({
							model : sampleModel
						});
						Lvl.dialogRegion.show(dialogView);
					});
				});
				view.on('samples:file:export', function(collectionId, selectedModels) {
					require([ 'apps/collection/export_sample/export_sample_view' ], function(EditView) {
						var samples = selectedModels.filter(function(element) {
							return element !== undefined && element !== null;
						});
						var dialogView = new EditView.Content({
							collection : new SampleModel.SampleCollection(samples),
							data_source : collectionId
						});
						Lvl.dialogRegion.show(dialogView);
					});
				});
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.CollectionApp.Samples.Controller;
});