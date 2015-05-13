/**
 * RequireJS module that defines the routes of the sub-application: analysis.
 */

define([ 'app', 'routefilter' ], function(Lvl) {
	Lvl.module('Routers.AnalysisApp', function(AnalysisAppRouter, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var Router = Backbone.Router.extend({
			routes : {
				'analysis' : 'showAnalysis',
				'analysis/:section' : 'showAnalysis',
				'analysis/:section/:subsection' : 'showAnalysis'
			},
			before : function() {
				if (!Lvl.config.isAuthenticated()) {
					Lvl.navigate('login/' + encodeURIComponent(Backbone.history.fragment) + '/unauthenticated', {
						trigger : true,
						replace : true
					});
					return false;
				}
				require([ 'apps/analysis/analysis_app' ], function() {
					Lvl.execute('set:active:header', 'workspace', 'analysis');
					Lvl.execute('set:active:footer', 'workspace');
					Lvl.startSubApp('AnalysisApp');
				});
				return true;
			},
			showAnalysis : function(section, subsection) {
				section = (section || 'pipelines').toLowerCase();
				if (section === 'pipelines') {
					Lvl.navigate('analysis/' + section, {
						trigger : false,
						replace : true
					});
					Lvl.execute('analysis:set:active', section);
				} else if (section === 'runs') {
					Lvl.execute('analysis:set:active', section, subsection);
				} else {
					Lvl.navigate('not-found', {
						trigger : true,
						replace : true
					});
				}
			}
		});
		Lvl.addInitializer(function() {
			var router = new Router();
		});
	});
});