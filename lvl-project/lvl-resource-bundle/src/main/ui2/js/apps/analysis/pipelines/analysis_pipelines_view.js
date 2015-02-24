/**
 * RequireJS module that defines the view: analysis->pipelines.
 */

define([ 'app', 'tpl!apps/analysis/pipelines/templates/analysis_pipelines', 'apps/config/marionette/styles/style', 'entities/workflow', 'pace',
		'backbone.oauth2', 'backgrid', 'backgrid-paginator', 'backgrid-select-all', 'backgrid-filter' ],
		function(Lvl, PipelinesTpl, Style, WorkflowModel, pace) {
			Lvl.module('AnalysisApp.Pipelines.View', function(View, Lvl, Backbone, Marionette, $, _) {
				'use strict';
				var columns = [
						{
							name : 'id',
							label : 'Identifier',
							cell : 'string'
						},
						{
							name : 'name',
							label : 'Name',
							editable : false,
							cell : 'string'
						},
						{
							name : 'description',
							label : 'Description',
							editable : false,
							cell : 'string'
						},
						{
							name : 'id',
							label : '',
							editable : false,
							cell : Backgrid.Cell.extend({
								render : function() {
									this.$el.empty();
									var rawValue = this.model.get(this.column.get('name'));
									var formattedValue = this.formatter.fromRaw(rawValue, this.model);
									if (formattedValue && typeof formattedValue === 'string') {
										this.$el.append('<a href="#" data-pipeline="' + formattedValue
												+ '" title="Run" class="text-muted"><i class="fa fa-play fa-fw"></i></i></a>');
									}
									this.delegateEvents();
									return this;
								}
							})
						} ];
				View.Content = Marionette.ItemView.extend({
					id : 'pipelines',
					template : PipelinesTpl,
					initialize : function() {
						this.listenTo(this.collection, 'request', this.displaySpinner);
						this.listenTo(this.collection, 'sync error', this.removeSpinner);
						this.grid = new Backgrid.Grid({
							columns : [ {
								name : '',
								cell : 'select-row',
								headerCell : 'select-all'
							} ].concat(columns),
							collection : this.collection,
							emptyText : 'No pipelines found'
						});
					},
					displaySpinner : function() {
						pace.restart();
						$('#grid-container').fadeTo('fast', 0.4);
					},
					removeSpinner : function() {
						pace.stop();
						$('#grid-container').fadeTo('fast', 1);
						$('html,body').animate({
							scrollTop : 0
						}, '500', 'swing');
					},
					events : {
						'click a[data-pipeline]' : 'runPipeline',
						'click a#uncheck-btn' : 'deselectAll',
					},
					runPipeline : function(e) {
						e.preventDefault();
						var target = $(e.target);
						var itemId = target.is('i') ? target.parent('a').get(0).getAttribute('data-pipeline') : target.getAttribute('data-pipeline');
						this.trigger('analysis:pipeline:run', itemId);
					},
					deselectAll : function(e) {
						e.preventDefault();
						this.grid.clearSelectedModels();
					},
					onBeforeRender : function() {
						require([ 'entities/styles' ], function() {
							var stylesLoader = new Style();
							stylesLoader.loadCss(Lvl.request('styles:backgrid:entities').toJSON());
							stylesLoader.loadCss(Lvl.request('styles:pace:entities').toJSON());
						});
					},
					onDestroy : function() {
						// don't remove the styles in order to enable them to be
						// reused
						pace.stop();
					},
					onRender : function() {
						var self = this;
						pace.start();

						var gridContainer = this.$('#grid-container');
						gridContainer.append(this.grid.render().el);

						var paginator = new Backgrid.Extension.Paginator({
							collection : this.collection,
							windowSize : 14,
							slideScale : 0.5,
							goBackFirstOnSort : true
						});

						gridContainer.after(paginator.render().el);

						$(paginator.el).css({
							'margin-top' : '20px'
						});

						var filter = new Backgrid.Extension.ServerSideFilter({
							collection : this.collection,
							name : 'q',
							placeholder : 'filter pipelines'
						});

						var filterToolbar = this.$('#grid-filter-toolbar');
						filterToolbar.append(filter.render().el);

						$(filter.el).addClass('pull-right lvl-filter-container');

						this.$('#hide-edition-toolbar-btn').click(function(event) {
							event.preventDefault();
							$('#edition-toolbar').hide();
							$('#show-edition-toolbar-btn').removeClass('hidden');
							$('#show-edition-toolbar-btn').show();
						});

						this.$('#show-edition-toolbar-btn').click(function(event) {
							event.preventDefault();
							$('#show-edition-toolbar-btn').hide();
							$('#edition-toolbar').show();
						});

						this.grid.clearSelectedModels();

						this.collection.fetch({
							reset : true
						});
					}
				});
			});
			return Lvl.AnalysisApp.Pipelines.View;
		});