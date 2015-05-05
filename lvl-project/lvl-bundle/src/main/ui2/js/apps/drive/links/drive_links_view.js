/**
 * RequireJS module that defines the view: drive->links.
 */

define(
		[ 'app', 'marionette', 'tpl!apps/drive/links/tpls/drive_links', 'tpl!apps/drive/links/tpls/toolbar_browse', 'tpl!common/search/tpls/search_term',
				'tpl!common/search/tpls/add_search_term', 'tpl!common/search/tpls/save_search', 'apps/config/marionette/styles/style',
				'apps/config/marionette/configuration', 'pace', 'moment', 'backbone.oauth2', 'backgrid', 'backgrid-paginator', 'backgrid-select-all',
				'backgrid-filter' ],
		function(Lvl, Marionette, LinksTpl, ToolbarTpl, SearchTermTpl, AddSearchTermTpl, SaveSearchTpl, Style, Configuration, pace, moment) {
			Lvl
					.module(
							'DriveApp.Links.View',
							function(View, Lvl, Backbone, Marionette, $, _) {
								'use strict';
								var config = new Configuration();
								var columns = [
										{
											name : 'openAccessLink',
											label : 'Secret',
											editable : false,
											cell : 'string'
										},
										{
											name : 'filename',
											label : 'Source',
											editable : false,
											cell : 'string'
										},
										{
											name : 'openAccessDate',
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
											name : 'openAccessLink',
											label : '',
											editable : false,
											cell : Backgrid.Cell
													.extend({
														render : function() {
															this.$el.empty();
															var rawValue = this.model.get(this.column.get('name'));
															var formattedValue = this.formatter.fromRaw(rawValue, this.model);
															if (formattedValue && typeof formattedValue === 'string') {
																this.$el
																		.append('<a href="'
																				+ config.get('service', '')
																				+ '/public/datasets/'
																				+ formattedValue
																				+ '" target="_blank" title="Download" class="text-muted"><i class="fa fa-download fa-fw"></i></a>');
															}
															this.delegateEvents();
															return this;
														}
													})
										},
										{
											name : 'openAccessLink',
											label : '',
											editable : false,
											cell : Backgrid.Cell.extend({
												render : function() {
													this.$el.empty();
													var rawValue = this.model.get(this.column.get('name'));
													var formattedValue = this.formatter.fromRaw(rawValue, this.model);
													if (formattedValue && typeof formattedValue === 'string') {
														this.$el.append('<a href="#" title="Shortened URL" class="text-muted" data-shortened-url="'
																+ formattedValue + '"><i class="fa fa-compress fa-fw"></i></a>');
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
								View.Content = Marionette.ItemView
										.extend({
											id : 'links',
											template : LinksTpl,
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
													emptyText : 'No links found'
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
													// setup search terms from
													// server response
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
												'click a[data-shortened-url]' : 'shortenUrl',
												'click a[data-remove]' : 'removeLink'
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
												var itemId = target.is('i') ? target.parent('a').get(0).getAttribute('data-search-term') : target
														.attr('data-search-term');
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
												require(
														[ 'common/growl' ],
														function(createGrowl) {
															createGrowl(
																	'Unsaved search',
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
												require([ 'apps/drive/links/tours/links_tour' ], function(tour) {
													tour();
												});
											},
											shortenUrl : function(e) {
												e.preventDefault();
												var self = this;
												var target = $(e.target);
												var itemId = target.is('i') ? target.parent('a').get(0).getAttribute('data-shortened-url') : target
														.attr('data-shortened-url');
												$.ajax({
													type : 'GET',
													dataType : 'text',
													crossDomain : true,
													url : config.get('service', '') + '/public/datasets/' + encodeURIComponent(itemId) + '/shortened_url',
													headers : config.authorizationHeader()
												}).done(function(data) {
													require([ 'common/info' ], function(infoDialog) {
														infoDialog('Shortened URL', '<a href="' + data + '" target="_blank">' + data + '</a>');
													});
												}).fail(function() {
													require([ 'common/alert' ], function(alertDialog) {
														alertDialog('Error', 'The shortened link cannot be created.');
													});
												});
											},
											removeLink : function(e) {
												e.preventDefault();
												var self = this;
												var target = $(e.target);
												var itemId = target.is('i') ? target.parent('a').get(0).getAttribute('data-remove') : target
														.attr('data-remove');
												var item = this.collection.get(itemId);
												item.oauth2_token = config.authorizationToken();
												require([ 'common/confirm' ], function(confirmDialog) {
													confirmDialog('Confirm deletion', 'This action will delete the selected public link. Are you sure?',
															function() {
																self.collection.remove(item);
																item.destroy({
																	success : function(e) {
																	},
																	error : function(e) {
																		require([ 'common/alert' ], function(alertDialog) {
																			alertDialog('Error', 'The public link cannot be removed.');
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
												// don't remove the styles in
												// order to enable them to be
												// reused
												pace.stop();
												this.stopListening();
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
													placeholder : 'filter links'
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
			return Lvl.DriveApp.Links.View;
		});