/**
 * RequireJS module that defines the controller: settings->instances.
 */

define([ 'app', 'entities/instance', 'apps/settings/instances/settings_instances_view' ], function(Lvl, InstanceModel, View) {
	Lvl.module('SettingsApp.Instances', function(Instances, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Instances.Controller = {
			showSection : function() {
				var view = new View.Content({
					collection : new InstanceModel.InstancePageableCollection({
						oauth2_token : Lvl.config.authorizationToken()
					})
				});
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.SettingsApp.Instances.Controller;
});