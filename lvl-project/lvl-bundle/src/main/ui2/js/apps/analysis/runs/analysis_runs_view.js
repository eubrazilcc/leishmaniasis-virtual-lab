/**
 * RequireJS module that defines the view: analysis->runs.
 */

define(
		[ 'app', 'tpl!apps/analysis/runs/tpls/analysis_runs', 'tpl!apps/e-compendium/browse/tpls/toolbar_browse', 'tpl!common/search/tpls/search_term',
				'tpl!common/search/tpls/add_search_term', 'tpl!common/search/tpls/save_search', 'apps/config/marionette/styles/style', 'entities/workflow_run',
				'apps/config/marionette/configuration', 'pace', 'moment', 'backbone.oauth2', 'backgrid', 'backgrid-paginator', 'backgrid-select-all',
				'backgrid-filter' ], function(Lvl, RunsTpl, ToolbarTpl, SearchTermTpl, AddSearchTermTpl, SaveSearchTpl, Style, WorkflowRunModel, Configuration,
				pace, moment) {
			Lvl.module('AnalysisApp.Runs.View', function(View, Lvl, Backbone, Marionette, $, _) {
				'use strict';
				var config = new Configuration();
				var columns = [
						{
							name : 'id',
							label : 'Identifier',
							editable : false,
							cell : Backgrid.Cell.extend({
								render : function() {
									this.$el.empty();
									var rawValue = this.model.get(this.column.get('name'));
									var formattedValue = this.formatter.fromRaw(rawValue, this.model);
									if (formattedValue && typeof formattedValue === 'string') {
										this.$el.append('<a href="/#analysis/runs/' + formattedValue + '" title="Open">' + formattedValue + '</a>');
									}
									this.delegateEvents();
									return this;
								}
							})
						},
						{
							name : 'workflowId',
							label : 'Workflow',
							editable : false,
							cell : 'string'
						},
						{
							name : 'invocationId',
							label : 'Invocation',
							editable : false,
							cell : 'string'
						},
						{
							name : 'submitted',
							label : 'Submitted',
							editable : false,
							cell : Backgrid.Cell.extend({
								render : function() {
									this.$el.empty();
									var rawValue = this.model.get(this.column.get('name'));
									var formattedValue = this.formatter.fromRaw(rawValue, this.model);
									if (formattedValue && typeof formattedValue === 'number') {
										this.$el.append(moment(formattedValue).format('MMM DD[,] YYYY [at] HH[:]mm'));
									}
									this.delegateEvents();
									return this;
								}
							})
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
										this.$el.append('<a href="#" title="Remove" class="text-muted" data-remove="' + formattedValue
												+ '"><i class="fa fa-times fa-fw"></i></a>');
									}
									this.delegateEvents();
									return this;
								}
							})
						} ];
				View.Content = Marionette.ItemView.extend({
					id : 'runs',
					template : RunsTpl,
					initialize : function() {
						this.listenTo(this.collection, 'request', this.displaySpinner);
						this.listenTo(this.collection, 'sync error remove', this.removeSpinner);
						this.grid = new Backgrid.Grid({
							columns : [ {
								name : '',
								cell : 'select-row',
								headerCell : 'select-all'
							} ].concat(columns),
							collection : this.collection,
							emptyText : 'No executions found'
						});
						// setup search
						Lvl.vent.on('search:form:submitted', this.searchLinks);
						// setup menu
						$('#lvl-floating-menu-toggle').show(0);
						$('#lvl-floating-menu').hide(0);
						$('#lvl-floating-menu').empty();
						$('#lvl-floating-menu').append(ToolbarTpl({}));
						$('a#uncheck-btn').on('click', {
							grid : this.grid
						}, this.deselectAll);
						$('button#lvl-feature-tour-btn').on('click', this.startTour);
					},
					displaySpinner : function() {
						pace.restart();
						$('#grid-container').fadeTo('fast', 0.4);
					},
					removeSpinner : function() {
						pace.stop();
						var self = this;
						$('#grid-container').fadeTo('fast', 1);
						$('html,body').animate({
							scrollTop : 0
						}, '500', 'swing', function() {
							// reset search terms
							var searchCont = $('#lvl-search-terms-container');
							searchCont.empty();
							// setup search terms from server response
							if (self.collection.formattedQuery && self.collection.formattedQuery.length > 0) {
								var i = 1;
								_.each(self.collection.formattedQuery, function(item) {
									searchCont.append(SearchTermTpl({
										sterm_id : 'sterm_' + (i++),
										sterm_text : item['term'],
										sterm_icon : Boolean(item['valid']) ? 'label-success' : 'label-warning',
										sterm_title : 'Remove'
									}));
								});
								searchCont.append(SearchTermTpl({
									sterm_id : 'sterm_-1',
									sterm_text : 'clear all',
									sterm_icon : 'label-danger',
									sterm_title : 'Remove All'
								}));
								searchCont.append(AddSearchTermTpl({}));
								searchCont.append(SaveSearchTpl({}));
								$('#lvl-search-terms').show('fast');
							} else {
								$('#lvl-search-terms').hide('fast');
							}
						});
					},
					events : {
						'click a[data-search-term]' : 'resetSearchTerms',
						'submit form#lvl-add-search-term-form' : 'addSearchTerm',
						'click div.lvl-savable' : 'handleClickSavable',
						'dragstart div.lvl-savable' : 'handleDragStart',
						'dragend div.lvl-savable' : 'handleDragEnd',
						'click a[data-remove]' : 'removeRun'
					},					
					deselectAll : function(e) {
						e.preventDefault();
						$('#lvl-floating-menu').hide('fast');
						e.data.grid.clearSelectedModels();
					},
					searchLinks : function(search) {
						var backgridFilter = $('form.backgrid-filter:first');
						backgridFilter.find('input:first').val(search);
						backgridFilter.submit();
					},
					resetSearchTerms : function(e) {
						e.preventDefault();
						var target = $(e.target);
						var search = '';
						var itemId = target.is('i') ? target.parent('a').get(0).getAttribute('data-search-term') : target.attr('data-search-term');
						if (itemId !== 'sterm_-1') {
							var searchCont = $('#lvl-search-terms-container');
							searchCont.find('a[data-search-term!="sterm_-1"]').each(function(i) {
								if ($(this).attr('data-search-term') !== itemId) {
									search += $(this).parent().text() + ' ';
								}
							});
						}
						this.searchLinks(search);
					},
					addSearchTerm : function(e) {
						e.preventDefault();
						var newTermInput = this.$('#lvl-add-search-term-input'), newTerm = newTermInput.val().trim();
						if (newTerm.length > 0) {
							this.$('li#lvl-add-search-term-container').hide();
							var searchCont = this.$('#lvl-search-terms-container');
							searchCont.append(SearchTermTpl({
								sterm_id : 'sterm_0',
								sterm_text : newTerm,
								sterm_icon : 'label-default',
								sterm_title : 'Remove'
							}));
							var search = '';
							searchCont.find('a[data-search-term!="sterm_-1"]').each(function(i) {
								search += $(this).parent().text() + ' ';
							});
							this.searchSequences(search);
						} else {
							newTermInput.val('');
						}
					},
					handleClickSavable : function(e) {
						require([ 'common/growl' ], function(createGrowl) {
							createGrowl('Unsaved search',
									'Start dragging the icon <i class="fa fa-bookmark-o"></i><sub><i class="fa fa-plus-circle"></i></sub> to open your saved items',
									false);
						});
					},
					handleDragStart : function(e) {
						var self = this;
						e.originalEvent.dataTransfer.setData('srcId', $(e.target).attr('data-savable-id'));
						e.originalEvent.dataTransfer.setData('savableType', 'saved_search');
						e.originalEvent.dataTransfer.setData('savable', JSON.stringify(new SavedSearchEntity.SavedSearch({
							type : 'sequences;' + self.data_source,
							description : '',
							search : self.collection.formattedQuery
						}).toJSON()));
						Lvl.vent.trigger('editable:items:dragstart');
					},
					handleDragEnd : function(e) {
						Lvl.vent.trigger('editable:items:dragend');
					},
					startTour : function(e) {
						e.preventDefault();
						require([ 'apps/analysis/runs/tours/runs_tour' ], function(tour) {
							tour();
						});
					},
					removeRun : function(e) {
						e.preventDefault();
						var self = this;
						var target = $(e.target);
						var itemId = target.is('i') ? target.parent('a').get(0).getAttribute('data-remove') : target.getAttribute('data-remove');
						var item = this.collection.get(itemId);
						item.oauth2_token = config.authorizationToken();
						require([ 'common/confirm' ], function(confirmDialog) {
							confirmDialog('Confirm deletion', 'This action will delete the products associated to the selected run. Are you sure?', function() {
								self.collection.remove(item);
								item.destroy({
									success : function(e) {
									},
									error : function(e) {
										require([ 'common/alert' ], function(alertDialog) {
											alertDialog('Error', 'The execution cannot be canceled.');
										});
									}
								});
							}, {
								btn_text : 'Delete'
							});
						});
					},
					onBeforeRender : function() {
						require([ 'entities/styles' ], function() {
							var stylesLoader = new Style();
							stylesLoader.loadCss(Lvl.request('styles:backgrid:entities').toJSON());
							stylesLoader.loadCss(Lvl.request('styles:pace:entities').toJSON());
						});
					},
					onDestroy : function() {
						// don't remove the styles in order to enable them to be reused
						pace.stop();
						this.stopListening();
						// remove all event handlers
						Lvl.vent.off('search:form:submitted');
						$('#lvl-search-form').unbind();
						$('button#lvl-feature-tour-btn').unbind();
						// clean menu
						$('#lvl-floating-menu').hide(0);
						$('#lvl-floating-menu-toggle').hide(0);
						$('#lvl-floating-menu').empty();
						// clean tour
						require([ 'hopscotch' ], function(hopscotch) {
							hopscotch.endTour();
						});
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
							placeholder : 'filter executions'
						});

						var filterToolbar = this.$('#grid-filter-toolbar');
						filterToolbar.append(filter.render().el);

						$(filter.el).addClass('hidden');

						this.grid.clearSelectedModels();

						this.collection.fetch({
							reset : true
						});
					}
				});
			});
			return Lvl.AnalysisApp.Runs.View;
		});