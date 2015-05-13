/**
 * RequireJS module that defines the view: access->legal->privacy policy viewer.
 */

define([ 'app', 'tpl!apps/access/legal/tpls/privacy_policy_viewer' ], function(Lvl, PrivacyPolicyTpl) {
	Lvl.module('AccessApp.PrivacyPolicy.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		View.Content = Marionette.ItemView.extend({
			template : PrivacyPolicyTpl			
		});
	});
	return Lvl.AccessApp.PrivacyPolicy.View;
});