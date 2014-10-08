/**
 * RequireJS module that defines the controller: collection->browse.
 */

define([ 'app', 'apps/config/marionette/configuration', 'entities/sequence', 'apps/collection/browse/collection_browse_view' ], function(Lvl, Configuration,
		SequenceModel, View) {
	Lvl.module('CollectionApp.Browse', function(Browse, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		Browse.Controller = {
			showSection : function() {
				var view = new View.Content({
					collection : new SequenceModel.SequencePageableCollection({
						oauth2_token : config.authorizationToken()
					})
				});
				view.on('sequences:link:create', function(selectedModels) {
					require([ 'apps/collection/link/link_view' ], function(EditView) {
						var sequences = selectedModels.filter(function(element) {
							return element !== undefined && element !== null;
						});
						var dialogView = new EditView.Content({
							collection : new SequenceModel.SequenceCollection(sequences)
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