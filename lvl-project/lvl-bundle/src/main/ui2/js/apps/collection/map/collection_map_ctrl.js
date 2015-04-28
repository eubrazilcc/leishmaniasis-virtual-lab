/**
 * RequireJS module that defines the controller: collection->map.
 */

define([ 'app', 'apps/config/marionette/configuration', 'apps/collection/map/collection_map_view' ], function(Lvl, Configuration, View) {
	Lvl.module('CollectionApp.Map', function(Map, Lvl, Backbone, Marionette, $, _) {
		'use strict';		
		Map.Controller = {
			showSection : function() {
				var view = new View.Content();
				view.on('sequences:view:sequence', function(accession) {
					require([ 'apps/collection/sequence_viewer/collection_sequence_viewer', 'entities/gb_sequence' ], function(SequenceView, GbSequenceModel) {
						var gbSequenceModel = new GbSequenceModel.GbSequence({
							'gbSeqPrimaryAccession' : accession
						});
						gbSequenceModel.oauth2_token = new Configuration().authorizationToken();
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
	return Lvl.CollectionApp.Map.Controller;
});