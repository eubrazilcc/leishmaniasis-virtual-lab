/**
 * RequireJS module that defines the view: admin->issues.
 */

define([ 'app', 'tpl!apps/admin/issues/tpls/admin_issues', 'tpl!apps/admin/issues/tpls/toolbar', 'tpl!common/search/tpls/search_term',
		'tpl!common/search/tpls/add_search_term', 'tpl!common/search/tpls/save_search', 'entities/issue', 'pace', 'moment', 'backbone.oauth2', 'backgrid',
		'backgrid-paginator', 'backgrid-select-all', 'backgrid-filter' ], function(Lvl, IssuesTpl, ToolbarTpl, SearchTermTpl, AddSearchTermTpl, SaveSearchTpl,
		SearchEntity, pace, moment) {
	Lvl.module('AdminApp.Issues.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var columns = [
				{
					name : 'status',
					label : 'Status',
					editable : false,
					cell : Backgrid.Cell.extend({
						render : function() {
							this.$el.empty();
							var rawValue = this.model.get(this.column.get('name'));
							if (rawValue !== undefined) {
								var color = 'label-default';
								if ('NEW' === rawValue) {
									color = 'label-info';
								} else if ('ASSIGNED' === rawValue) {
									color = 'label-success';
								} else if ('CLOSED' === rawValue) {
									color = 'label-danger';
								} else if ('REOPENED' === rawValue) {
									color = 'label-warning';
								}
								this.$el.append('<span class="label ' + color + '">' + rawValue + '</span> ');
							}
							this.delegateEvents();
							return this;
						}
					})
				},
				{
					name : 'opened',
					label : 'Opened',
					editable : false,
					cell : Backgrid.Cell.extend({
						render : function() {
							this.$el.empty();
							var rawValue = this.model.get(this.column.get('name'));
							var formattedValue = this.formatter.fromRaw(rawValue, this.model);
							if (formattedValue && typeof formattedValue === 'number') {
								this.$el.append('<i class="fa fa-clock-o fa-fw"></i> ' + moment(formattedValue).format('MMM DD[,] YYYY [at] HH[:]mm'));
							}
							this.delegateEvents();
							return this;
						}
					})
				},
				{
					name : 'closed',
					label : 'Closed',
					editable : false,
					cell : Backgrid.Cell.extend({
						render : function() {
							this.$el.empty();
							var rawValue = this.model.get(this.column.get('name'));
							var formattedValue = this.formatter.fromRaw(rawValue, this.model);
							if (formattedValue && typeof formattedValue === 'number') {
								this.$el.append('<i class="fa fa-history fa-fw"></i> ' + moment(formattedValue).format('MMM DD[,] YYYY [at] HH[:]mm'));
							}
							this.delegateEvents();
							return this;
						}
					})
				},
				{
					name : 'email',
					label : 'Reporter',
					editable : false,
					cell : 'string'
				},
				{
					name : 'browser',
					label : 'Browser',
					editable : false,
					cell : 'string'
				},
				{
					name : 'system',
					label : 'System',
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
					name : 'screenshot',
					label : 'Screenshot',
					editable : false,
					cell : 'string'
				},
				{
					name : 'id',
					label : '',
					editable : false,
					sortable : false,
					cell : Backgrid.Cell.extend({
						render : function() {
							this.$el.empty();
							var rawValue = this.model.get(this.column.get('name'));
							var formattedValue = this.formatter.fromRaw(rawValue, this.model);
							if (formattedValue && typeof formattedValue === 'string') {
								this.$el.append('<a href="#" title="Remove" data-remove-issue="' + formattedValue
										+ '" class="text-muted"><i class="fa fa-times fa-fw"></i></a>');
							}
							this.delegateEvents();
							return this;
						}
					})
				} ];
		View.Content = Marionette.ItemView.extend({
			id : 'issues',
			template : IssuesTpl,
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
					emptyText : 'No issues found'
				});
				// setup search
				Lvl.vent.on('search:form:submitted', this.searchSavedIssues);
				// setup menu
				$('#lvl-floating-menu-toggle').show(0);
				$('#lvl-floating-menu').hide(0);
				$('#lvl-floating-menu').empty();
				$('#lvl-floating-menu').append(ToolbarTpl({}));
				$('a#uncheck-btn').on('click', {
					grid : this.grid
				}, this.deselectAll);
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
				'click a[data-remove-issue]' : 'removeIssue'
			},
			deselectAll : function(e) {
				e.preventDefault();
				$('#lvl-floating-menu').hide('fast');
				e.data.grid.clearSelectedModels();
			},
			searchIssues : function(search) {
				var backgridFilter = $('form.backgrid-filter:first');
				backgridFilter.find('input:first').val(search);
				// TODO backgridFilter.submit();
				// TODO
				require([ 'common/growl' ], function(createGrowl) {
					createGrowl('Operation unavailable', 'Search feature is coming soon. Stay tuned!', false);
				});
				// TODO
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
				this.searchIssues(search);
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
				e.originalEvent.dataTransfer.setData('savableType', 'issue');
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
			removeIssue : function(e) {
				e.preventDefault();
				var self = this;
				var target = $(e.target);
				var itemId = target.is('i') ? target.parent('a').get(0).getAttribute('data-remove-issue') : target.attr('data-remove-issue');
				var item = this.collection.get(itemId);
				item.oauth2_token = Lvl.config.authorizationToken();
				require([ 'common/confirm' ], function(confirmDialog) {
					confirmDialog('Confirm deletion', 'This action will delete the selected issue. Are you sure?', function() {
						self.collection.remove(item);
						item.destroy({
							success : function(e) {
							},
							error : function(e) {
								require([ 'common/alert' ], function(alertDialog) {
									alertDialog('Error', 'The issue cannot be removed.');
								});
							}
						});
					}, {
						btn_text : 'Delete'
					});
				});
			},			
			onDestroy : function() {
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
					placeholder : 'filter citations'
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
	return Lvl.AdminApp.Issues.View;
});