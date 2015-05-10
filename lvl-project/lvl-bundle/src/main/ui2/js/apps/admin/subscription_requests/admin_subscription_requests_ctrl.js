/**
 * RequireJS module that defines the controller: admin->subscription_requests.
 */

define([ 'app', 'apps/config/marionette/configuration', 'entities/subscription_request', 'apps/admin/subscription_requests/admin_subscription_requests_view' ],
		function(Lvl, Configuration, SubscReqEntity, View) {
			Lvl.module('AdminApp.SubscReqs', function(SubscReqs, Lvl, Backbone, Marionette, $, _) {
				'use strict';
				var config = new Configuration();
				SubscReqs.Controller = {
					showSection : function() {
						var view = new View.Content({
							collection : new SubscReqEntity.SubscrReqPageableCollection({
								oauth2_token : config.authorizationToken()
							})
						});
						Lvl.mainRegion.currentView.tabContent.show(view);
						return View.Content.id;
					}
				}
			});
			return Lvl.AdminApp.SubscReqs.Controller;
		});