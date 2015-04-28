/**
 * RequireJS module that defines the controller: settings->instances.
 */

define([ 'app', 'apps/config/marionette/configuration', 'entities/instance', 'apps/settings/instances/settings_instances_view' ], function(Lvl, Configuration,
		InstanceModel, View) {
	Lvl.module('SettingsApp.Instances', function(Instances, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		Instances.Controller = {
			showSection : function() {
				var view = new View.Content({
					collection : new InstanceModel.InstancePageableCollection({
						oauth2_token : config.authorizationToken()
					})
				});
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.SettingsApp.Instances.Controller;
});