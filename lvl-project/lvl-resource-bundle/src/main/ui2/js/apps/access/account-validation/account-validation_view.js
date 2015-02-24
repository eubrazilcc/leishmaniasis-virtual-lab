/**
 * RequireJS module that defines the view: access->account-validation.
 */

define([ 'marionette', 'tpl!apps/access/account-validation/templates/account-validation' ], function(Marionette, AccountValidationTpl) {
	return {
		Content : Marionette.ItemView.extend({
			template : AccountValidationTpl,
			templateHelpers : {
				accountEmail : function() {
					return this.email ? decodeURIComponent(this.email) : '';
				},
				validationCode : function() {
					return this.code ? decodeURIComponent(this.code) : '';
				}
			},
			onBeforeRender : function() {
				$('body').addClass('lvl-login-body');
			},
			onDestroy : function() {
				$('body').removeClass('lvl-login-body');
			}
		})
	};
});