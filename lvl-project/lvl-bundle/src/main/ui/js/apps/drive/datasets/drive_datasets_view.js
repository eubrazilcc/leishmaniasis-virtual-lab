/**
 * RequireJS module that defines the view: drive->datasets.
 */

define([ 'app', 'marionette', 'tpl!apps/drive/datasets/tpls/drive_datasets', 'tpl!apps/drive/datasets/tpls/toolbar_browse',
		'tpl!common/search/tpls/search_term', 'tpl!common/search/tpls/add_search_term', 'tpl!common/search/tpls/save_search', 'pace', 'moment', 'filesize',
		'backbone.oauth2', 'backgrid', 'backgrid-paginator', 'backgrid-select-all', 'backgrid-filter' ], function(Lvl, Marionette, DatasetsTpl, ToolbarTpl,
		SearchTermTpl, AddSearchTermTpl, SaveSearchTpl, pace, moment, filesize) {
	Lvl.module('DriveApp.Datasets.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var columns = [
				{
					name : 'filename',
					label : 'Name',
					editable : false,
					cell : 'string'
				},
				{
					name : 'tags',
					label : 'Tags',
					editable : false,
					cell : Backgrid.Cell.extend({
						render : function() {
							this.$el.empty();
							var metadata = this.model.get('metadata');
							var rawValue = metadata ? metadata[this.column.get('name')] : undefined;
							if (rawValue !== undefined) {
								var names = '';
								for (var i = 0; i < rawValue.length; i++) {
									var color = 'label-default';
									if ('pipeline_product' === rawValue[i]) {
										color = 'label-success';
									} else if ('fasta' === rawValue[i]) {
										color = 'label-info';
									}
									names += '<span class="label ' + color + '">' + rawValue[i] + '</span> ';
								}
								this.$el.append(names.trim());
							}
							this.delegateEvents();
							return this;
						}
					})
				},
				{
					name : 'length',
					label : 'Size',
					editable : false,
					cell : 'integer',
					formatter : _.extend({}, Backgrid.CellFormatter.prototype, {
						fromRaw : function(rawValue, model) {
							return rawValue ? filesize(rawValue) : rawValue;
						}
					})
				},
				{
					name : 'contentType',
					label : 'Type',
					editable : false,
					cell : Backgrid.Cell.extend({
						render : function() {
							this.$el.empty();
							var rawValue = this.model.get(this.column.get('name'));
							var formattedValue = this.formatter.fromRaw(rawValue, this.model);
							if (formattedValue && typeof formattedValue === 'string') {
								var image = '', type = '';
								switch (formattedValue.trim().toLowerCase()) {
								case 'text/plain':
									image = 'fa-file-text-o';
									type = 'text';
									break;
								case 'application/gzip':
									image = 'fa-file-zip-o';
									type = 'zip';
									break;
								default:
									image = 'fa-file-o';
									type = 'unknown';
									break;
								}
								this.$el.append('<i class="fa ' + image + ' fa-fw"></i> ' + type);
							}
							this.delegateEvents();
							return this;
						}
					})
				},
				{
					name : 'uploadDate',
					label : 'Created',
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
					name : 'filename',
					label : 'Description',
					editable : false,
					cell : Backgrid.Cell.extend({
						render : function() {
							this.$el.empty();
							var metadata = this.model.get('metadata');
							var rawValue = metadata !== undefined ? metadata.description : undefined;
							var formattedValue = this.formatter.fromRaw(rawValue, this.model);
							if (formattedValue && typeof formattedValue === 'string') {
								this.$el.append(formattedValue);
							}
							this.delegateEvents();
							return this;
						}
					})
				},
				{
					name : 'filename',
					label : '',
					editable : false,
					cell : Backgrid.Cell.extend({
						render : function() {
							this.$el.empty();
							var rawValue = this.model.get(this.column.get('name'));
							var formattedValue = this.formatter.fromRaw(rawValue, this.model);
							if (formattedValue && typeof formattedValue === 'string') {
								this.$el.append('<a href="#" data-dataset="' + formattedValue
										+ '" title="Share" class="text-muted"><i class="fa fa-share fa-fw"></i></a>');
							}
							this.delegateEvents();
							return this;
						}
					})
				},
				{
					name : 'filename',
					label : '',
					editable : false,
					cell : Backgrid.Cell.extend({
						render : function() {
							this.$el.empty();
							var rawValue = this.model.get(this.column.get('name'));
							var formattedValue = this.formatter.fromRaw(rawValue, this.model);
							if (formattedValue && typeof formattedValue === 'string') {
								this.$el.append('<a href="' + Lvl.config.get('service', '') + '/datasets/objects/~/' + formattedValue + "/download?"
										+ Lvl.config.authorizationQuery()
										+ '" target="_blank" title="Download" class="text-muted"><i class="fa fa-download fa-fw"></i></a>');
							}
							this.delegateEvents();
							return this;
						}
					})
				},
				{
					name : 'filename',
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
			id : 'datasets',
			template : DatasetsTpl,
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
					emptyText : 'No datasets found'
				});
				Lvl.vent.on('datasets:file:uploaded', this.render);
				// setup search
				Lvl.vent.on('search:form:submitted', this.searchDatasets);
				// setup menu
				$('#lvl-floating-menu-toggle').show(0);
				$('#lvl-floating-menu').hide(0);
				$('#lvl-floating-menu').empty();
				$('#lvl-floating-menu').append(ToolbarTpl({}));
				$('a#upload-btn').on('click', {
					view : this
				}, this.uploadFile);
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
				'click a[data-dataset]' : 'createLink',
				'click a[data-remove]' : 'removeDataset'
			},
			uploadFile : function(e, data) {
				e.preventDefault();
				$('#lvl-floating-menu').hide('fast');
				e.data.view.trigger('datasets:file:upload');
			},
			deselectAll : function(e) {
				e.preventDefault();
				$('#lvl-floating-menu').hide('fast');
				e.data.grid.clearSelectedModels();
			},			
			searchDatasets : function(search) {
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
				require([ 'apps/drive/datasets/tours/datasets_tour' ], function(tour) {
					tour();
				});
			},
			createLink : function(e) {
				e.preventDefault();
				var self = this;
				var target = $(e.target);
				var itemId = target.is('i') ? target.parent('a').get(0).getAttribute('data-dataset') : target.getAttribute('data-dataset');
				require([ 'common/confirm', 'entities/link' ], function(confirmDialog, LinkModel) {
					confirmDialog('Confirm public link creation', 'Create a public link to the dataset: ' + itemId + '?', function() {
						var newLink = new LinkModel.LinkCreate();
						newLink.oauth2_token = Lvl.config.authorizationToken();
						newLink.save({
							'filename' : itemId
						}, {
							success : function(model, resp, options) {
								require([ 'common/growl' ], function(createGrowl) {
									var anchor = $('<a>', {
										href : options.xhr.getResponseHeader('Location')
									})[0];
									var filename = anchor.pathname.substring(anchor.pathname.lastIndexOf('/') + 1);
									createGrowl('New link created', filename
											+ ' <a href="/#drive/links"><i class="fa fa-arrow-circle-right fa-fw"></i> links</a>', false);
								});
							},
							error : function(model, resp, options) {
								require([ 'common/alert' ], function(alertDialog) {
									alertDialog('Error', 'Failed to create link.');
								});
							}
						});
						self.collection.add([ newLink ]);
					}, {
						icon : 'fa-info-circle',
						icon_color : 'text-info',
						btn_text : 'Create'
					});
				});
			},
			removeDataset : function(e) {
				e.preventDefault();
				var self = this;
				var target = $(e.target);
				var itemId = target.is('i') ? target.parent('a').get(0).getAttribute('data-remove') : target.attr('data-remove');
				var item = this.collection.get(itemId);
				item.oauth2_token = Lvl.config.authorizationToken();
				require([ 'common/confirm' ], function(confirmDialog) {
					confirmDialog('Confirm deletion', 'This action will delete the selected dataset. Are you sure?', function() {
						self.collection.remove(item);
						item.destroy({
							success : function(e) {
							},
							error : function(e) {
								require([ 'common/alert' ], function(alertDialog) {
									alertDialog('Error', 'The dataset cannot be removed.');
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
				Lvl.vent.off('datasets:file:uploaded');
				Lvl.vent.off('search:form:submitted');
				$('#lvl-search-form').unbind();
				$('a#upload-btn').unbind();
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
					placeholder : 'filter datasets'
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
	return Lvl.DriveApp.Datasets.View;
});