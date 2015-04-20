/**
 * RequireJS module that defines the view: footer->show.
 */

define([ 'app', 'tpl!apps/footer/show/templates/footer' ], function(Lvl, FooterTpl) {
	Lvl.module('FooterApp.Show.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		View.Footer = Marionette.ItemView.extend({
			id : 'default',
			template : FooterTpl,
			events : {
				'click a#privacy_policy_btn' : 'showPrivacyPolicy',
				'click a#terms_and_conditions_btn' : 'showTermsAndConditions'
			},
			showPrivacyPolicy : function(e) {
				e.preventDefault();
				this.trigger('access:view:privacy_policy');
			},
			showTermsAndConditions : function(e) {
				e.preventDefault();
				this.trigger('access:view:terms_and_conditions');
			}
		});
	});
	return Lvl.FooterApp.Show.View;
});