/**
 * RequireJS module that defines the view: analysis->runs_item_tree_viewer.
 */

define([ 'app', 'tpl!apps/analysis/trevi/tpls/trevi', 'entities/workflow_run', 'pace', 'jquery.panzoom', 'backbone.oauth2' ], function(Lvl, TreeTpl,
		WfRunModel, pace) {
	Lvl.module('AnalysisApp.RunsItemTree.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		View.Content = Marionette.ItemView.extend({
			id : 'runs_item_tree',
			template : TreeTpl,
			initialize : function() {
				this.listenTo(this.model, 'request', this.displaySpinner);
				this.listenTo(this.model, 'sync error', this.removeSpinner);
				this.listenTo(this.model, 'change', this.render);
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
				'click a#btn-zoom-in' : 'zoomInTree',
				'click a#btn-zoom-out' : 'zoomOutTree',
				'click a#btn-restore' : 'restoreTree'
			},
			zoomInTree : function(e) {
				e.preventDefault();
				var self = this;
				self.$el.find('img#svg-container').panzoom('zoom');
			},
			zoomOutTree : function(e) {
				e.preventDefault();
				var self = this;
				self.$el.find('img#svg-container').panzoom('zoom', true);
			},
			restoreTree : function(e) {
				e.preventDefault();
				var self = this;
				self.$el.find('img#svg-container').panzoom('reset');
			},
			onDestroy : function() {
				pace.stop();
				this.stopListening();
			},
			onRender : function() {
				var self = this;
				pace.start();
				self.model.fetch({
					reset : true
				}).done(function() {
					self.$el.find('img#svg-container').panzoom({});
				});
			},
			onShow : function() {
				var params = Lvl.flashed();
				if (!$.isEmptyObject(params) && params.id && params.path) {
					
					// TODO
					console.log(JSON.stringify(params));
					// TODO
					
				}
			}
		});
	});
	return Lvl.AnalysisApp.RunsItemTree.View;
});