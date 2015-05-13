/**
 * RequireJS module that defines the controller: admin->subscription_requests.
 */

define([ 'app', 'entities/subscription_request', 'apps/admin/subscription_requests/admin_subscription_requests_view' ], function(Lvl, SubscReqEntity, View) {
	Lvl.module('AdminApp.SubscReqs', function(SubscReqs, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		SubscReqs.Controller = {
			showSection : function() {
				var view = new View.Content({
					collection : new SubscReqEntity.SubscrReqPageableCollection({
						oauth2_token : Lvl.config.authorizationToken()
					})
				});
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.AdminApp.SubscReqs.Controller;
});