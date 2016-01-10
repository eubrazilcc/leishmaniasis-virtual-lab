/**
 * RequireJS module that defines the controller: curation->submitted_citations.
 */

define([ 'app', 'entities/pending_citation', 'apps/curation/submitted_citations/curation_submitted_citations_view' ], function(Lvl, PendingCitationModel, View) {
	Lvl.module('CurationApp.SubmittedCitations', function(SubmittedCitations, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		SubmittedCitations.Controller = {
			showSection : function() {
				var view = new View.Content({
					collection : new PendingCitationModel.PendingCitationPageableCollection({
						oauth2_token : Lvl.config.authorizationToken()
					})
				});
				view.on('pending:resolve:record', function(collectionId, item) {
					require([ 'apps/curation/submission_resolver/curation_submission_resolver' ], function(ResolverView) {
						var dialogView = new ResolverView.Content({
							section : 'citations',
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
	return Lvl.CurationApp.SubmittedCitations.Controller;
});