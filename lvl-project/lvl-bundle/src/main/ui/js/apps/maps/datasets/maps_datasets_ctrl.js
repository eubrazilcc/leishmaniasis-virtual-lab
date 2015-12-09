/**
 * RequireJS module that defines the controller: maps->datasets.
 */

define([ 'app', 'apps/maps/datasets/maps_datasets_view' ], function(Lvl, View) {
	Lvl.module('MapsApp.Datasets', function(Datasets, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Datasets.Controller = {
			showSection : function() {
				var view = new View.Content();
				view.on('sequences:view:sequence', function(accession) {
					require([ 'apps/collection/sequence_viewer/collection_sequence_viewer', 'entities/gb_sequence' ], function(SequenceView, GbSequenceModel) {
						var gbSequenceModel = new GbSequenceModel.GbSequence({
							'gbSeqPrimaryAccession' : accession
						});
						gbSequenceModel.oauth2_token = Lvl.config.authorizationToken();
						var dialogView = new SequenceView.Content({
							model : gbSequenceModel
						});
						Lvl.dialogRegion.show(dialogView);
					});
				});
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.MapsApp.Datasets.Controller;
});