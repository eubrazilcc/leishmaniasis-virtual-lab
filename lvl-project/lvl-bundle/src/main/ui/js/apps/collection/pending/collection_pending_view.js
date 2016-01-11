/**
 * RequireJS module that defines the view: collection->pending.
 */

define([ 'app', 'tpl!apps/collection/pending/tpls/collection_pending', 'tpl!apps/collection/pending/tpls/toolbar_pending', 'tpl!common/search/tpls/search_term',
		'tpl!common/search/tpls/add_search_term', 'tpl!common/search/tpls/save_search', 'entities/pending_sequence', 'entities/saved_search', 'entities/identifier', 'pace', 'moment',
		'common/country_names', 'backbone.oauth2', 'backgrid', 'backgrid-paginator', 'backgrid-select-all', 'backgrid-filter', 'common/ext/backgrid_ext' ], function(Lvl, PendingTpl,
		ToolbarTpl, SearchTermTpl, AddSearchTermTpl, SaveSearchTpl, PendingSequenceEntity, SavedSearchEntity, IdentifierEntity, pace, moment, mapCn) {
	Lvl.module('CollectionApp.Pending.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var columns = [
				{
					name : 'id',
					label : 'Id',
					editable : false,
					cell : 'string'
				},
				{
					name : 'sample',
					label : 'Modified',
					editable : false,
					cell : Backgrid.Cell.extend({
						render : function() {
							this.$el.empty();
							var rawValue = this.model.get(this.column.get('name'));
							var formattedValue = this.formatter.fromRaw(rawValue, this.model);
							if (formattedValue && typeof formattedValue['modified'] === 'object') {
								this.$el.append('<i class="fa fa-clock-o fa-fw"></i> ' + moment(formattedValue['modified'].content, "YYYY-MM-DDTHH:mm:ss").format('MMM DD[,] YYYY [at] HH[:]mm'));
							}
							this.delegateEvents();
							return this;
						}
					})
				},				
				{
					name : 'status',
					label : 'Status',
					editable : false,
					cell : Backgrid.Cell.extend({
						render : function() {
							this.$el.empty();
							var rawValue = this.model.get(this.column.get('name'));
							var formattedValue = this.formatter.fromRaw(rawValue, this.model);							
							if (formattedValue && typeof formattedValue === 'string') {
								var status = '';
								switch (formattedValue.trim().toUpperCase()) {								
								case 'NEW':
									status = '<i class="fa fa-file-o fa-fw"></i> ' + formattedValue;
									break;
								case 'ASSIGNED':
									status = '<i class="fa fa-user fa-fw"></i> ' + formattedValue;
									break;
								case 'ACCEPTED':
									status = '<i class="fa fa-check fa-fw"></i> ' + formattedValue;
									break;
								case 'CLOSED':
									status = '<i class="fa fa-archive fa-fw"></i> ' + formattedValue;
									break;
								case 'REOPENED':
									status = '<i class="fa fa-file fa-fw"></i> ' + formattedValue;
									break;
								default:
									status = 'Unsubmitted';
									break;
								}
								this.$el.append(status);
							} else this.$el.append('<span class="label label-info">Unsubmitted</span>');
							this.delegateEvents();
							return this;
						}
					})
				},
				{
					name : 'resolution',
					label : 'Resolution',
					editable : false,
					cell : Backgrid.Cell.extend({
						render : function() {
							this.$el.empty();
							var rawValue = this.model.get(this.column.get('name'));
							var formattedValue = this.formatter.fromRaw(rawValue, this.model);
							if (formattedValue && typeof formattedValue === 'string') {
								var resolution = '';
								switch (formattedValue.trim().toUpperCase()) {
								case 'ACCEPTED':
									resolution = '<span class="label label-success">' + formattedValue + '</span>';
									break;
								case 'INVALID':
									resolution = '<span class="label label-danger">' + formattedValue + '</span>';
									break;
								case 'DUPLICATE':
									resolution = '<span class="label label-warning">' + formattedValue + '</span>';
									break;
								default:
									resolution = '';
									break;
								}
								this.$el.append(resolution);
							}
							this.delegateEvents();
							return this;
						}
					})
				},
				{
					name : 'sample',
					label : 'Collection',
					editable : false,
					cell : Backgrid.Cell.extend({
						render : function() {
							this.$el.empty();
							var rawValue = this.model.get(this.column.get('name'));
							var formattedValue = this.formatter.fromRaw(rawValue, this.model);
							if (formattedValue && typeof formattedValue['institutionCode'] === 'string') {
								this.$el.append(formattedValue['institutionCode']);
							}
							this.delegateEvents();
							return this;
						}
					})
				},
				{
					name : 'sample',
					label : 'Country',
					editable : false,
					cell : Backgrid.Cell.extend({
						render : function() {
							this.$el.empty();
							var rawValue = this.model.get(this.column.get('name'));							
							var formattedValue = this.formatter.fromRaw(rawValue, this.model);
							if (formattedValue && typeof formattedValue['country'] === 'string') {
								this.$el.append(formattedValue['country']);
							}							
							this.delegateEvents();
							return this;
						}
					})
				},				
				{
					name : 'sample',
					label : 'Scientific name',
					editable : false,
					cell : Backgrid.Cell.extend({
						render : function() {
							this.$el.empty();
							var rawValue = this.model.get(this.column.get('name'));							
							var formattedValue = this.formatter.fromRaw(rawValue, this.model);
							if (formattedValue && typeof formattedValue['scientificName'] === 'string') {
								this.$el.append(formattedValue['scientificName']);
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
								this.$el.append('<a href="#" title="Open" data-pending-seq_id="' + formattedValue
										+ '" class="text-muted"><i class="fa fa-eye fa-fw"></i></a>');
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
								this.$el.append('<a href="#" data-pending-share-seq_id="' + formattedValue
										+ '" title="Share" class="text-muted"><i class="fa fa-share-alt fa-fw"></i></a>');
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
								this.$el.append('<a href="#" data-pending-submit-seq_id="' + formattedValue
										+ '" title="Submit" class="text-muted"><i class="fa fa-paper-plane fa-fw"></i></a>');
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
								this.$el.append('<a href="#" title="Remove" class="text-muted" data-pending-remove-seq_id="' + formattedValue
										+ '"><i class="fa fa-times fa-fw"></i></a>');
							}
							this.delegateEvents();
							return this;
						}
					})
				} ];
		View.Content = Marionette.ItemView.extend({
			id : 'pending',
			template : PendingTpl,
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
					emptyText : 'No pending sequences found'
				});
				// setup search
				Lvl.vent.on('search:form:submitted', this.searchPendingSequences);
				// setup menu
				$('#lvl-floating-menu-toggle').show(0);
				$('#lvl-floating-menu').hide(0);
				$('#lvl-floating-menu').empty();
				$('#lvl-floating-menu').append(ToolbarTpl({
					isSanflies : 'sandflies' === this.data_source,
					isLeishmania : 'leishmania' === this.data_source
				}));				
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
				'click a[data-pending-seq_id]' : 'showPendingSequenceRecord',
				'click a[data-pending-share-seq_id]' : 'sharePendingSequenceRecord',
				'click a[data-pending-submit-seq_id]' : 'submitPendingSequenceRecord',
				'click a[data-pending-remove-seq_id]' : 'removePendingSequenceRecord'
			},			
			deselectAll : function(e) {
				e.preventDefault();
				$('#lvl-floating-menu').hide('fast');
				$('.select-all-header-cell > input:first').prop('checked', false).change();				
			},
			searchPendingSequences : function(search) {
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
				this.searchPendingSequences(search);
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
					this.searchPendingSequences(search);
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
					type : 'collection;pending;' + self.data_source,
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
				require([ 'apps/collection/pending/tours/collection_tour' ], function(tour) {
					tour();
				});
			},
			showPendingSequenceRecord : function(e) {
				e.preventDefault();
				var self = this;
				var target = $(e.target);
				var itemId = target.is('i') ? target.parent('a').get(0).getAttribute('data-pending-seq_id') : target.attr('data-pending-seq_id');
				this.trigger('pending:view:record', self.collection.data_source, itemId);
			},
			sharePendingSequenceRecord : function(e) {
				e.preventDefault();
				var self = this;
				var target = $(e.target);
				var itemId = target.is('i') ? target.parent('a').get(0).getAttribute('data-pending-share-seq_id') : target.attr('data-pending-share-seq_id');
				this.trigger('pending:share:record', self.collection.data_source, itemId);
			},
			submitPendingSequenceRecord : function(e) {
				e.preventDefault();
				var self = this;
				var target = $(e.target);
				var itemId = target.is('i') ? target.parent('a').get(0).getAttribute('data-pending-submit-seq_id') : target.attr('data-pending-submit-seq_id');				
				var item = self.collection.get(itemId);				
				if (!item.get('status') || item.get('status') === 'CLOSED') {
					require([ 'common/confirm' ], function(confirmDialog) {
						confirmDialog('Confirm sumission', 'This action will start a evaluation process in order to include the selected record in the public collection. Continue?', function() {
							item.set({ 'status' : !item.get('status') ? 'NEW' : 'REOPENED' });
							pace.restart();
							var jqxhr = $.ajax({
								url : Lvl.config.get('service.url') + '/pending/' + self.data_source + '/~/' + item.get('id'),
								type: 'PUT',
								crossDomain : true,
								contentType : 'application/json',
								headers : Lvl.config.authorizationHeader(),							
								data : JSON.stringify(item)
							}).always(function() {
								pace.stop();
							}).done(function(data, textStatus, request) {							
								if (200 === request.status || 204 === request.status) {
									// self.collection.set([ item ]);
								} else {
									require([ 'common/alert' ], function(alertDialog) {
										alertDialog('Error', 'The server response was not OK.');
									});
								}
							}).fail(function(jqXHR, textStatus, errorThrown) {
								require([ 'common/alert' ], function(alertDialog) {
									alertDialog('Error', 'The record cannot be submitted.');
								});
							});
						}, {
							btn_text : 'Submit'
						});
					});
				} else {
					require([ 'common/alert' ], function(alertDialog) {
						alertDialog('Error', 'The record cannot be resubmitted until is closed.');
					});
				}				
			},
			removePendingSequenceRecord : function(e) {
				e.preventDefault();
				var self = this;
				var target = $(e.target);
				var itemId = target.is('i') ? target.parent('a').get(0).getAttribute('data-pending-remove-seq_id') : target.attr('data-pending-remove-seq_id');				
				var item = this.collection.get(itemId);
				item.set('dataSource', self.data_source);
				item.oauth2_token = Lvl.config.authorizationToken();
				require([ 'common/confirm' ], function(confirmDialog) {
					confirmDialog('Confirm deletion', 'This action will delete the selected record. Are you sure?', function() {
						self.collection.remove(item);
						item.destroy({
							success : function(e) {
							},
							error : function(e) {
								require([ 'common/alert' ], function(alertDialog) {
									alertDialog('Error', 'The record cannot be removed.');
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
						&& (params.get('type') === 'collection;pending;' + _self.data_source)) {
					var search =  ''; // TODO 'organism:' + this.data_source;
					_.each(params.get('search'), function(item) {
						search += item.term;
					});
					_self.searchPendingSequences(search);
				} else {
					this.collection.fetch({
						reset : true
					});
				}
			}
		});
	});
	return Lvl.CollectionApp.Pending.View;
});