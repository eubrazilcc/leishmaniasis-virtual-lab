/**
 * RequireJS module that defines the view: e-compendium->pending.
 */

define([ 'marionette', 'tpl!apps/e-compendium/pending/tpls/e-compendium_pending' ], function(Marionette, PendingTpl) {
	return {
		Content : Marionette.ItemView.extend({
			id : 'pending',
			template : PendingTpl
		})
	};
});