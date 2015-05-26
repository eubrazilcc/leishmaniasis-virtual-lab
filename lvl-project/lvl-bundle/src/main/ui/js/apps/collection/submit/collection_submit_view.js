/**
 * RequireJS module that defines the view: collection->submit.
 */

define([ 'marionette', 'tpl!apps/collection/submit/tpls/collection_submit' ], function(Marionette, SubmitTpl) {
	return {
		Content : Marionette.ItemView.extend({
			id : 'submit',
			template : SubmitTpl
		})
	};
});