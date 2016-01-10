/**
 * RequireJS module that defines the controller: curation->submitted_sequences.
 */

define([ 'app', 'entities/pending_sequence', 'apps/curation/submitted_sequences/curation_submitted_sequences_view' ], function(Lvl, PendingSequenceModel, View) {
	Lvl.module('CurationApp.SubmittedSequences', function(SubmittedSequences, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		SubmittedSequences.Controller = {
			showSection : function(id) {				
				var collectionId = id || 'sandflies';
				var view = new View.Content({
					collection : new PendingSequenceModel.PendingSequencePageableCollection({
						oauth2_token : Lvl.config.authorizationToken(),
						data_source : collectionId,
						curator : true
					})
				});
				view.on('pending:view:record', function(collectionId, sampleId) {
					require([ 'apps/collection/pending_viewer/collection_pending_viewer', 'entities/pending_sequence' ], function(PendingSequenceView, PendingSeqModel) {
						var pendingSeqModel = new PendingSeqModel.PendingSequence({
							'dataSource' : collectionId,
							'id' : sampleId
						});
						pendingSeqModel.oauth2_token = Lvl.config.authorizationToken();
						var dialogView = new PendingSequenceView.Content({
							model : pendingSeqModel
						});
						Lvl.dialogRegion.show(dialogView);
					});
				});
				view.on('pending:resolve:record', function(collectionId, item) {
					require([ 'apps/curation/submission_resolver/curation_submission_resolver' ], function(ResolverView) {
						var dialogView = new ResolverView.Content({
							section : 'sequences',
							collectionId : collectionId,
							item : item
						});
						Lvl.dialogRegion.show(dialogView);
					});
				});
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.CurationApp.SubmittedSequences.Controller;
});