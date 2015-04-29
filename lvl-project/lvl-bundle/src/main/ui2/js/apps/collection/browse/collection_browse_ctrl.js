/**
 * RequireJS module that defines the controller: collection->browse.
 */

define([ 'app', 'apps/config/marionette/configuration', 'entities/sequence', 'apps/collection/browse/collection_browse_view' ], function(Lvl, Configuration,
		SequenceModel, View) {
	Lvl.module('CollectionApp.Browse', function(Browse, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Browse.Controller = {
			showSection : function(id) {
				var collectionId = id || 'sandflies';
				var view = new View.Content({
					collection : new SequenceModel.SequencePageableCollection({
						oauth2_token : new Configuration().authorizationToken(),
						data_source : collectionId
					})
				});
				view.on('sequences:view:sequence', function(collectionId, accession) {
					require([ 'apps/collection/sequence_viewer/collection_sequence_viewer', 'entities/gb_sequence' ], function(SequenceView, GbSequenceModel) {
						var gbSequenceModel = new GbSequenceModel.GbSequence({
							'dataSource' : collectionId,
							'gbSeqPrimaryAccession' : accession
						});
						gbSequenceModel.oauth2_token = new Configuration().authorizationToken();
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
	return Lvl.CollectionApp.Browse.Controller;
});