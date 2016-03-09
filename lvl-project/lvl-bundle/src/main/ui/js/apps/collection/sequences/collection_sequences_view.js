/**
 * RequireJS module that defines the view: collection->sequences.
 */

define([ 'app', 'tpl!apps/collection/sequences/tpls/collection_sequences', 'tpl!apps/collection/sequences/tpls/toolbar_sequences', 'tpl!common/search/tpls/search_term',
		'tpl!common/search/tpls/add_search_term', 'tpl!common/search/tpls/save_search', 'entities/sequence', 'entities/saved_search', 'entities/identifier', 'pace',
		'common/country_names', 'backbone.oauth2', 'backgrid', 'backgrid-paginator', 'backgrid-select-all', 'backgrid-filter', 'common/ext/backgrid_ext' ], function(Lvl, SequencesTpl,
		ToolbarTpl, SearchTermTpl, AddSearchTermTpl, SaveSearchTpl, SequenceEntity, SavedSearchEntity, IdentifierEntity, pace, mapCn) {
	Lvl.module('CollectionApp.Sequences.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var columns = [
				{
					name : 'dataSource',
					label : 'Source',
					editable : false,
					cell : 'string'
				},
				{
					name : 'definition',
					label : 'Definition',
					editable : false,
					cell : 'string'
				},
				{
					name : 'accession',
					label : 'Accession',
					editable : false,
					cell : 'string'
				},
				{
					name : 'length',
					label : 'Length',
					editable : false,
					cell : 'integer',
					formatter : _.extend({}, Backgrid.CellFormatter.prototype, {
						innerFormatter : new Backgrid.NumberFormatter({
							decimals : 0
						}),
						fromRaw : function(rawValue, model) {
							var self = this;
							return self.innerFormatter.fromRaw(rawValue) + " bp";
						}
					})
				},
				{
					name : 'gene',
					label : 'Gene',
					editable : false,
					cell : Backgrid.Cell.extend({
						render : function() {
							this.$el.empty();
							var rawValue = this.model.get(this.column.get('name'));
							if (rawValue !== undefined) {
								var names = '';
								for (var i = 0; i < rawValue.length; i++) {
									names += rawValue[i] + ' ';
								}
								this.$el.append(names.trim());
							}
							this.delegateEvents();
							return this;
						}
					})
				},
				{
					name : 'organism',
					label : 'Organism',
					editable : false,
					cell : 'string'
				},
				{
					name : 'locale',
					label : 'Country',
					editable : false,
					cell : Backgrid.Cell.extend({
						render : function() {
							this.$el.empty();
							var rawValue = this.model.get(this.column.get('name'));
							var formattedValue = this.formatter.fromRaw(rawValue, this.model);
							if (formattedValue && typeof formattedValue === 'string') {
								var twoLetterCode = formattedValue.split("_")[1];
								var code2 = twoLetterCode ? twoLetterCode.toUpperCase() : '';
								var countryName = mapCn[code2];
								if (countryName) {
									this.$el.append('<a href="#" data-country-code2="' + code2.toLowerCase() + '"><span class="flag-icon flag-icon-'
											+ code2.toLowerCase() + '" title="' + countryName + '"></span><span class="hidden-xs"> ' + code2 + '</span></a>');
								}
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
					sortable : false,
					cell : Backgrid.Cell.extend({
						render : function() {
							this.$el.empty();
							var rawValue = this.model.get(this.column.get('name'));
							var formattedValue = this.formatter.fromRaw(rawValue, this.model);
							if (formattedValue && typeof formattedValue === 'string') {
								this.$el.append('<a href="#" title="Open" data-seq_id="' + formattedValue
										+ '" class="text-muted"><i class="fa fa-eye fa-fw"></i></a>');
							}
							this.delegateEvents();
							return this;
						}
					})
				} ];
		View.Content = Marionette.ItemView.extend({
			id : 'sequences',
			template : SequencesTpl,
			initialize : function() {
				this.data_source = this.collection.data_source || 'sandflies';
				this.listenTo(this.collection, 'request', this.displaySpinner);
				this.listenTo(this.collection, 'sync error', this.removeSpinner);				
				this.listenTo(this.collection, 'backgrid:select-all', this.selectAllHandler);
				this.grid = new Backgrid.Grid({
					columns : [ {
						name : '',
						cell : 'select-row',
						headerCell : 'select-all'
					} ].concat(columns),
					collection : this.collection,
					emptyText : 'No sequences found'
				});
				// setup search
				Lvl.vent.on('search:form:submitted', this.searchSequences);
				Lvl.vent.on('search:advanced_form:opened', this.showAdvancedSearchForm, this);
				// setup menu
				$('#lvl-floating-menu-toggle').show(0);
				$('#lvl-floating-menu').hide(0);
				$('#lvl-floating-menu').empty();
				$('#lvl-floating-menu').append(ToolbarTpl({
					isSanflies : 'sandflies' === this.data_source,
					isLeishmania : 'leishmania' === this.data_source
				}));
				$('a#export-btn').on('click', {
					view : this
				}, this.exportFile);
				$('a#uncheck-btn').on('click', {
					grid : this.grid
				}, this.deselectAll);
				$('button#lvl-feature-tour-btn').on('click', this.startTour);
			},
			displaySpinner : function() {
				pace.restart();
				$('#grid-container').fadeTo('fast', 0.4);
			},
			removeSpinner : function(reset) {
				pace.stop();
				var self = this;				
				$('#grid-container').fadeTo('fast', 1);				
				if (typeof reset === 'boolean' ? reset : true) {					
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
				}
			},			
			selectAllHandler : function(data, checked) {				
				var self = this;
				if (checked === true) {
					self.displaySpinner();
					var identifierModel = new IdentifierEntity.Identifier({
						'dataSource' : self.collection.data_source,
						'queryParam' : $('form.backgrid-filter:first').find('input:first').val()
					});
					identifierModel.oauth2_token = Lvl.config.authorizationToken();
					identifierModel.fetch({
						reset : true
					}).done(function() {						
						if (identifierModel.get('hash') === self.collection.lvlOpHash) {
							self.grid.addSelectedIds(identifierModel.get('identifiers'));
						}
					}).fail(function() {
						require([ 'common/growl' ], function(createGrowl) {
							createGrowl('Operation failed', 'Cannot select all sequences at this time. Please try again later.', false);
						});
					}).always(function() {
						self.removeSpinner(false);
					});
				} else self.grid.deselectAll();
			},
			events : {
				'click a[data-search-term]' : 'resetSearchTerms',
				'submit form#lvl-add-search-term-form' : 'addSearchTerm',
				'click div.lvl-savable' : 'handleClickSavable',
				'dragstart div.lvl-savable' : 'handleDragStart',
				'dragend div.lvl-savable' : 'handleDragEnd',
				'click a[data-country-code2]' : 'filterByCountry',
				'click a[data-seq_id]' : 'showSequenceRecord'				
			},
			exportFile : function(e, data) {
				e.preventDefault();
				var selectedModels = e.data.view.grid.getAllSelectedIds();				
				if (selectedModels && selectedModels.length > 0) {
					if (selectedModels.length <= 1000) { // limit to 1000 sequences
						$('#lvl-floating-menu').hide('fast');
						e.data.view.trigger('sequences:file:export', e.data.view.collection.data_source, selectedModels);
					} else {
						$('#lvl-floating-menu').hide('0');
						require([ 'common/growl' ], function(createGrowl) {
							createGrowl('Export size limit reached', 'Export tool is currently limited to 1000 sequences.', false);
						});
					}				
				} else {
					$('#lvl-floating-menu').hide('0');
					require([ 'common/growl' ], function(createGrowl) {
						createGrowl('No sequences selected', 'Select at least one sequence to be exported', false);
					});
				}
			},
			deselectAll : function(e) {
				e.preventDefault();
				$('#lvl-floating-menu').hide('fast');
				$('.select-all-header-cell > input:first').prop('checked', false).change();				
			},
			searchSequences : function(search) {
				var backgridFilter = $('form.backgrid-filter:first');
				backgridFilter.find('input:first').val(search);
				backgridFilter.submit();
			},
			showAdvancedSearchForm : function() {
				this.trigger('sequences:advanced:search', this.data_source);
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
				this.searchSequences(search);
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
			filterByCountry : function(e) {
				e.preventDefault();
				var _self = this;
				var target = $(e.target);
				var countryCode = target.is('span') || target.is('img') ? target.parent('a').get(0).getAttribute('data-country-code2') : target
						.attr('data-country-code2');
				this.searchSequences('locale:_' + countryCode.toUpperCase());
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
					type : 'collection;sequences;' + self.data_source,
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
				require([ 'apps/collection/sequences/tours/collection_tour' ], function(tour) {
					tour();
				});
			},
			showSequenceRecord : function(e) {
				e.preventDefault();
				var self = this;
				var target = $(e.target);
				var itemId = target.is('i') ? target.parent('a').get(0).getAttribute('data-seq_id') : target.attr('data-seq_id');
				this.trigger('sequences:view:sequence', self.collection.data_source, itemId);
			},
			onDestroy : function() {
				pace.stop();
				this.stopListening();
				// remove all event handlers
				Lvl.vent.off('search:form:submitted');
				$('#lvl-search-form').unbind();
				$('a#export-btn').unbind();
				$('a#uncheck-btn').unbind();
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
					placeholder : 'filter sequences'
				});

				var filterToolbar = this.$('#grid-filter-toolbar');
				filterToolbar.append(filter.render().el);

				$(filter.el).addClass('hidden');

				this.grid.clearSelectedModels();
			},
			onShow : function() {
				var _self = this;
				var params = Lvl.flashed();
				if (!$.isEmptyObject(params) && (params instanceof SavedSearchEntity.SavedSearch)
						&& (params.get('type') === 'collection;sequences;' + _self.data_source)) {
					var search = '';
					_.each(params.get('search'), function(item) {
						search += item.term;
					});
					_self.searchSequences(search);
				} else {
					this.collection.fetch({
						reset : true
					});
				}
			}
		});
	});
	return Lvl.CollectionApp.Sequences.View;
});