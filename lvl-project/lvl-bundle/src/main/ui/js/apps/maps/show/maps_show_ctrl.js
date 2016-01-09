/**
 * RequireJS module that defines the controller: maps->show.
 */

define([ 'app', 'apps/maps/show/maps_show_view' ], function(Lvl, View) {
	Lvl.module('MapsApp.Show', function(Show, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Show.Controller = {
			showMaps : function() {
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
                Lvl.mainRegion.show(view);
			}
		}
	});
	return Lvl.MapsApp.Show.Controller;
});