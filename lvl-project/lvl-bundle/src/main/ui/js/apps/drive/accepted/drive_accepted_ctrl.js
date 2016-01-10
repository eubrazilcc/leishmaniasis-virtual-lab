/**
 * RequireJS module that defines the controller: drive->accepted.
 */

define([ 'app', 'entities/obj_accepted', 'apps/drive/accepted/drive_accepted_view' ], function(Lvl, ObjAcceptedModel, View) {
	Lvl.module('DriveApp.Accepted', function(Accepted, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Accepted.Controller = {
			showSection : function() {
				var self = this;
				var view = new View.Content({
					collection : new ObjAcceptedModel.ObjectAcceptedPageableCollection({
						oauth2_token : Lvl.config.authorizationToken()
					})
				});
				view.on('drive:view:obj_accepted', function(item) {
					var collection;
					switch (item.get('collection')) {
					case 'sandflyPending':
						collection = 'sandflies';
						self.showPendingSequence(collection, item.get('itemId'));
						break;
					case 'leishmaniaPending':
						collection = 'leishmania';
						self.showPendingSequence(collection, item.get('itemId'));
						break;
					default:
						require([ 'common/alert' ], function(alertDialog) {
							alertDialog('Error', 'No viewer available for the requested collection: ' + item.get('collection'));
						});
						break;
					}			
				});
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			},
			showPendingSequence : function(collection, itemId) {
				require([ 'apps/collection/pending_viewer/collection_pending_viewer', 'entities/pending_sequence' ], function(PendingSequenceView, PendingSeqModel) {
					var pendingSeqModel = new PendingSeqModel.PendingSequence({
						'dataSource' : collection,
						'id' : itemId
					});
					pendingSeqModel.oauth2_token = Lvl.config.authorizationToken();
					var dialogView = new PendingSequenceView.Content({
						model : pendingSeqModel
					});
					Lvl.dialogRegion.show(dialogView);
				});
			}
		}
	});
	return Lvl.DriveApp.Accepted.Controller;
});