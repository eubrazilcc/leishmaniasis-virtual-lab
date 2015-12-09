/**
 * RequireJS module that defines the view: statistics->samples.
 */

define([ 'marionette', 'tpl!apps/statistics/samples/tpls/statistics_samples' ], function(Marionette, SamplesTpl) {
	return {
		Content : Marionette.ItemView.extend({
			id : 'samples',
			template : SamplesTpl
		})
	};
});