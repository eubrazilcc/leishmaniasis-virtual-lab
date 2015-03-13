/**
 * RequireJS module that defines the view: collection->browse.
 */

define([ 'app', 'tpl!apps/collection/browse/templates/collection_browse', 'tpl!apps/collection/browse/templates/toolbar_browse',
		'apps/config/marionette/styles/style', 'entities/sequence', 'pace', 'common/country_names', 'backbone.oauth2', 'backgrid', 'backgrid-paginator',
		'backgrid-select-all', 'backgrid-filter' ], function(Lvl, BrowseTpl, ToolbarTpl, Style, SequenceModel, pace, mapCn) {
	Lvl.module('CollectionApp.Browse.View', function(View, Lvl, Backbone, Marionette, $, _) {
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
									this.$el.append('<a href="/#collection/map/country/' + code2.toLowerCase() + '"><img src="img/blank.gif" class="flag flag-'
											+ code2.toLowerCase() + '" alt="' + countryName + '" /><span class="hidden-xs"> ' + countryName + '</span></a>');
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
			id : 'browse',
			template : BrowseTpl,
			initialize : function() {
				this.data_source = this.collection.data_source || 'sandflies';
				this.listenTo(this.collection, 'request', this.displaySpinner);
				this.listenTo(this.collection, 'sync error', this.removeSpinner);
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
				'click a[data-seq_id]' : 'showSequenceRecord'
			},
			exportFile : function(e, data) {
				e.preventDefault();
				var selectedModels = e.data.view.grid.getSelectedModels();
				if (selectedModels && selectedModels.length > 0) {
					$('#lvl-floating-menu').hide('fast');
					e.data.view.trigger('sequences:file:export', e.data.view.collection.data_source, selectedModels);
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
				e.data.grid.clearSelectedModels();
			},
			searchSequences : function(search) {
				
				// TODO
				console.log('SUBMITTED IN COLLECTION: ' + search);
				// TODO

				/*
                $('#form-id :input:enabled:visible:first')
                <form class="backgrid-filter form-search">
                  <span class="search">&nbsp;</span><input type="search" placeholder="filter citations" name="q">
                  <a class="clear" data-backgrid-action="clear" href="#" style="display: none;">×</a>
                </form>
				*/
				
			},
			showSequenceRecord : function(e) {
				e.preventDefault();
				var self = this;
				var target = $(e.target);
				var itemId = target.is('i') ? target.parent('a').get(0).getAttribute('data-seq_id') : target.attr('data-seq_id');
				this.trigger('sequences:view:sequence', self.collection.data_source, itemId);
			},
			onBeforeRender : function() {
				require([ 'entities/styles' ], function() {
					var stylesLoader = new Style();
					stylesLoader.loadCss(Lvl.request('styles:backgrid:entities').toJSON());
					stylesLoader.loadCss(Lvl.request('styles:pace:entities').toJSON());
					stylesLoader.loadCss(Lvl.request('styles:flags:entities').toJSON());
				});
			},
			onDestroy : function() {
				// don't remove the styles in order to enable them to be reused
				pace.stop();
				// remove all event handlers
				
				// TODO : unbind from trigger
				
				$('#lvl-search-form').unbind();
				$('a#export-btn').unbind();
				$('a#uncheck-btn').unbind();
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
					placeholder : 'filter sequences'
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
	return Lvl.CollectionApp.Browse.View;
});