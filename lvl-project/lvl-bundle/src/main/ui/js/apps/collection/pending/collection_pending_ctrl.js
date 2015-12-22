/**
 * RequireJS module that defines the controller: collection->pending.
 */

define([ 'app', 'entities/pending_sequence', 'apps/collection/pending/collection_pending_view' ], function(Lvl, PendingSequenceModel, View) {
	Lvl.module('CollectionApp.Pending', function(Pending, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Pending.Controller = {
			showSection : function(id) {				
				var collectionId = id || 'sandflies';
				var view = new View.Content({
					collection : new PendingSequenceModel.PendingSequencePageableCollection({
						oauth2_token : Lvl.config.authorizationToken(),
						data_source : collectionId
					})
				});
				/* TODO view.on('sequences:view:sequence', function(collectionId, accession) {
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
				}); */
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.CollectionApp.Pending.Controller;
});