/**
 * RequireJS module that defines the view: e-compendium->stats.
 */

define([ 'marionette', 'tpl!apps/e-compendium/stats/tpls/e-compendium_stats' ], function(Marionette, StatsTpl) {
	return {
		Content : Marionette.ItemView.extend({
			id : 'stats',
			template : StatsTpl
		})
	};
});