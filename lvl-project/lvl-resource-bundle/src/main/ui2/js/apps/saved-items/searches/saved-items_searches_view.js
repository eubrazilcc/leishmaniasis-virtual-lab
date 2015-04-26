/**
 * RequireJS module that defines the view: saved-items->searches.
 */

define([ 'marionette', 'tpl!apps/saved-items/searches/tpls/saved-items_searches' ], function(Marionette, SearchesTpl) {
	return {
		Content : Marionette.ItemView.extend({
			id : 'searches',
			template : SearchesTpl
		})
	};
});