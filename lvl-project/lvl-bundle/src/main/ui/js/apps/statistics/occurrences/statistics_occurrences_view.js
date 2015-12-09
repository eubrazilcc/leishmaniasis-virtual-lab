/**
 * RequireJS module that defines the view: statistics->occurrences.
 */

define([ 'marionette', 'tpl!apps/statistics/occurrences/tpls/statistics_occurrences' ], function(Marionette, OccurrencesTpl) {
	return {
		Content : Marionette.ItemView.extend({
			id : 'occurrences',
			template : OccurrencesTpl
		})
	};
});