/**
 * RequireJS module that defines the view: e-compendium->submit.
 */

define([ 'marionette', 'tpl!apps/e-compendium/submit/tpls/e-compendium_submit' ], function(Marionette, SubmitTpl) {
	return {
		Content : Marionette.ItemView.extend({
			id : 'submit',
			template : SubmitTpl
		})
	};
});