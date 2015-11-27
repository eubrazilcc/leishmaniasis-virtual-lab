/**
 * RequireJS module that defines the view: collection->submit.
 */

define([ 'marionette', 'tpl!apps/collection/submit/tpls/collection_submit', 'chance' ], function(Marionette, SubmitTpl, Chance) {
	return {
		Content : Marionette.ItemView.extend({
			id : 'submit',
			template : SubmitTpl,
			templateHelpers : function() {
				return {					
					pendingId : function() {
						return 'user-' + new Chance().string({
							length : 8,
							pool : 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
						});
					}					
				}
			}
		})
	};
});