/**
 * RequireJS module that defines the controller: collection->unsequenced.
 */

define([ 'app', 'entities/sequence', 'apps/collection/unseq/collection_unseq_view' ], function(Lvl, SequenceModel, View) {
	Lvl.module('CollectionApp.Unseq', function(Unseq, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Unseq.Controller = {
			showSection : function(id) {
				var collectionId = id || 'sandflies';
				var view = new View.Content({
					collection : new SequenceModel.SequencePageableCollection({
						oauth2_token : Lvl.config.authorizationToken(),
						data_source : collectionId
					})
				});
				view.on('sequences:view:sequence', function(collectionId, accession) {
					require([ 'apps/collection/sequence_viewer/collection_sequence_viewer', 'entities/gb_sequence' ], function(SequenceView, GbSequenceModel) {
						var gbSequenceModel = new GbSequenceModel.GbSequence({
							'dataSource' : collectionId,
							'gbSeqPrimaryAccession' : accession
						});
						gbSequenceModel.oauth2_token = Lvl.config.authorizationToken();
						var dialogView = new SequenceView.Content({
							model : gbSequenceModel
						});
						Lvl.dialogRegion.show(dialogView);
					});
				});
				view.on('sequences:file:export', function(collectionId, selectedModels) {
					require([ 'apps/collection/export/export_view' ], function(EditView) {
						var sequences = selectedModels.filter(function(element) {
							return element !== undefined && element !== null;
						});
						var dialogView = new EditView.Content({
							collection : new SequenceModel.SequenceCollection(sequences),
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
	return Lvl.CollectionApp.Unseq.Controller;
});