/**
 * RequireJS module that defines the controller: maps->show.
 */

define([ 'app', 'apps/maps/show/maps_show_view' ], function(Lvl, View) {
	Lvl.module('MapsApp.Show', function(Show, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Show.Controller = {
			showMaps : function() {
				var view = new View.Content();
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
                Lvl.mainRegion.show(view);
			}
		}
	});
	return Lvl.MapsApp.Show.Controller;
});