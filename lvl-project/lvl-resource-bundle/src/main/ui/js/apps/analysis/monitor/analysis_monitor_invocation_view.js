/**
 * RequireJS module that defines the view: analysis->monitor_invocation.
 */

define([ 'app', 'tpl!apps/analysis/monitor/templates/analysis_monitor_invocation', 'apps/config/marionette/configuration' ], function(Lvl, MonitorTpl,
		Configuration) {
	Lvl.module('AnalysisApp.Monitor.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		View.Content = Marionette.ItemView.extend({
			template : MonitorTpl,
			initialize : function() {				
				var self = this;
				this.timer = setInterval(function() {
					self.model.fetch().done(function() {

						// TODO
						console.log('UPDATED');
						// TODO

					});
				}, 10000);
			},
			onClose : function() {
				clearInterval(this.timer);

				// TODO
				console.log('VIEW CLOSED');
				// TODO

			}
		});
	});
	return Lvl.AnalysisApp.Monitor.View;
});