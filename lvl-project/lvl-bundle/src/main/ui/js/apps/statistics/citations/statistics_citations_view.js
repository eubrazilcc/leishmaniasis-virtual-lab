/**
 * RequireJS module that defines the view: statistics->citations.
 */

define([ 'marionette', 'tpl!apps/statistics/citations/tpls/statistics_citations' ], function(Marionette, CitationsTpl) {
	return {
		Content : Marionette.ItemView.extend({
			id : 'citations',
			template : CitationsTpl
		})
	};
});