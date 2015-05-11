/**
 * RequireJS module that defines the controller: admin->issues.
 */

define([ 'app', 'apps/config/marionette/configuration', 'entities/issue', 'apps/admin/issues/admin_issues_view' ], function(Lvl, Configuration, IssueEntity,
		View) {
	Lvl.module('AdminApp.Issues', function(Issues, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		Issues.Controller = {
			showSection : function() {
				var view = new View.Content({
					collection : new IssueEntity.IssuePageableCollection({
						oauth2_token : config.authorizationToken()
					})
				});
				Lvl.mainRegion.currentView.tabContent.show(view);
				return View.Content.id;
			}
		}
	});
	return Lvl.AdminApp.Issues.Controller;
});