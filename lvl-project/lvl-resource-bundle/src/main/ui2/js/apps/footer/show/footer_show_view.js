/**
 * RequireJS module that defines the view: footer->show.
 */

define([ 'marionette', 'tpl!apps/footer/show/templates/footer' ], function(Marionette, FooterTpl) {
	return {
		Footer : Marionette.ItemView.extend({
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
		})
	};
});