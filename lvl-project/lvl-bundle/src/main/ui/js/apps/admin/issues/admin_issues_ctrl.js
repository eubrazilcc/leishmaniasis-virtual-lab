/**
 * RequireJS module that defines the controller: admin->issues.
 */

define([ 'app', 'entities/issue', 'apps/admin/issues/admin_issues_view' ], function(Lvl, IssueEntity, View) {
	Lvl.module('AdminApp.Issues', function(Issues, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Issues.Controller = {
			showSection : function() {
				var view = new View.Content({
					collection : new IssueEntity.IssuePageableCollection({
						oauth2_token : Lvl.config.authorizationToken()
					})
				});
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.AdminApp.Issues.Controller;
});