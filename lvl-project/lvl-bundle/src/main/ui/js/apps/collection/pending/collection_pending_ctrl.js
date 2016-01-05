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
				view.on('pending:share:record', function(collectionId, itemId) {
					require([ 'apps/collection/pending_shares_viewer/collection_pending_shares_viewer', 'entities/obj_granted' ], function(SharedPendingSequenceView, ObjGrantedModel) {
						var dialogView = new SharedPendingSequenceView.Content({
							collection : new ObjGrantedModel.ObjectGrantedPageableCollection({
								oauth2_token : Lvl.config.authorizationToken(),
								collectionId : collectionId,
								itemId : itemId
							})
						});
						Lvl.dialogRegion.show(dialogView);
					});
				});				
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.CollectionApp.Pending.Controller;
});