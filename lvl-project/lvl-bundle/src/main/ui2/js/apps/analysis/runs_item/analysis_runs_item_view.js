/**
 * RequireJS module that defines the view: analysis->runs_item.
 */

define([ 'app', 'tpl!apps/analysis/runs_item/templates/analysis_runs_item', 'apps/config/marionette/styles/style', 'entities/workflow_run',
		'apps/config/marionette/configuration', 'pace', 'backbone.oauth2' ], function(Lvl, RunItemTpl, Style, WorkflowRunModel, Configuration, pace) {
	Lvl.module('AnalysisApp.RunsItem.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		View.Content = Marionette.ItemView.extend({
			id : 'runs',
			template : RunItemTpl,
			templateHelpers : function() {
				return {
					statusClass : function() {
						var clazz = 'default';
						if (!this.status || !this.status.status) {
							return clazz;
						}						
						switch (this.status.status) {
						case 'Queued':
							clazz = 'default';
							break;
						case 'Running':
							clazz = 'primary';
							break;
						case 'Finished':
							clazz = 'success';
							break;
						case 'ExecutionError':
							clazz = 'danger';
							break;
						case 'Unknown':
							clazz = 'warning';
							break;
						}
						return clazz;
					}
				};
			},
			initialize : function() {
				this.listenTo(this.model, 'request', this.displaySpinner);
				this.listenTo(this.model, 'sync error', this.removeSpinner);
				this.listenTo(this.model, 'change', this.render);
				var self = this;
				this.timer = setInterval(function() {
					self.updateModel();
				}, 2000);
			},
			displaySpinner : function() {
				pace.restart();
				$('#item-container').fadeTo('fast', 0.4);
			},
			removeSpinner : function() {
				pace.stop();
				$('#item-container').fadeTo('fast', 1);
				$('html,body').animate({
					scrollTop : 0
				}, '500', 'swing');
			},
			events : {
				'click a#hide-edition-toolbar-btn' : 'hideToolbar',
				'click a#show-edition-toolbar-btn' : 'showToolbar',
				'click a[data-open]' : 'openRun',
				'click a#refresh-btn' : 'refreshRun'
			},
			hideToolbar : function(e) {
				e.preventDefault();
				this.$('#edition-toolbar').hide();
				this.$('#show-edition-toolbar-btn').removeClass('hidden');
				this.$('#show-edition-toolbar-btn').show();
			},
			showToolbar : function(e) {
				e.preventDefault();
				this.$('#show-edition-toolbar-btn').hide();
				this.$('#edition-toolbar').show();
			},
			openRun : function(e) {
				e.preventDefault();
				var self = this;
				var target = $(e.target);
				var productId = target.attr('data-open');
				self.trigger('analysis:pipeline:product:show', productId);
			},
			refreshRun : function(e) {
				e.preventDefault();
				var self = this;
				self.updateModel();
			},
			onBeforeRender : function() {
				require([ 'entities/styles' ], function() {
					var stylesLoader = new Style();
					stylesLoader.loadCss(Lvl.request('styles:pace:entities').toJSON());
				});
			},
			onDestroy : function() {
				this.terminateTimer();
				pace.stop();
			},
			onRender : function() {
				var self = this;
				pace.start();
				self.updateModel();
			},
			updateModel : function() {
				var self = this;
				self.model.fetch({
					reset : true
				}).done(function() {
					var status = self.model.get('status');
					if (status && (status.status === 'Finished' || status.status === 'ExecutionError' || status.status === 'Unknown')) {
						self.terminateTimer();
					}
				});
			},
			terminateTimer : function() {
				clearInterval(this.timer);
				// console.log('runs_item timer destroy');
			}
		});
	});
	return Lvl.AnalysisApp.RunsItem.View;
});