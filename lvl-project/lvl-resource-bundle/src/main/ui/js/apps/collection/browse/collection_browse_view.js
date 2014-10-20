/**
 * RequireJS module that defines the view: collection->browse.
 */

define([ 'app', 'tpl!apps/collection/browse/templates/collection_browse', 'apps/config/marionette/styles/style', 'entities/sequence', 'pace',
		'common/country_names', 'backbone.oauth2', 'backgrid', 'backgrid-paginator', 'backgrid-select-all', 'backgrid-filter' ], function(Lvl, BrowseTpl,
		Style, SequenceModel, pace, mapCn) {
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
											+ code2.toLowerCase() + '" alt="' + countryName + '" /> ' + countryName + '</a>');
								}
							}
							this.delegateEvents();
							return this;
						}
					})
				},
				{
					name : 'accession',
					label : '',
					editable : false,
					cell : Backgrid.Cell.extend({
						render : function() {
							this.$el.empty();
							var rawValue = this.model.get(this.column.get('name'));
							var formattedValue = this.formatter.fromRaw(rawValue, this.model);
							if (formattedValue && typeof formattedValue === 'string') {
								this.$el.append('<a href="#" title="Open" data-accession="' + formattedValue
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
				'click a#link-btn' : 'createLink',
				'click a#uncheck-btn' : 'deselectAll',
				'click a[data-accession]' : 'showGenBankRecord'
			},
			createLink : function(e) {
				e.preventDefault();
				var selectedModels = this.grid.getSelectedModels();
				this.trigger('sequences:link:create', selectedModels);
			},
			deselectAll : function(e) {
				e.preventDefault();
				this.grid.clearSelectedModels();
			},
			showGenBankRecord : function(e) {
				e.preventDefault();
				var self = this;
				var target = $(e.target);
				var itemId = target.is('i') ? target.parent('a').get(0).getAttribute('data-accession') : target.attr('data-accession');
				this.trigger('sequences:view:sequence', itemId);
			},
			onBeforeRender : function() {
				require([ 'entities/styles' ], function() {
					var stylesLoader = new Style();
					stylesLoader.loadCss(Lvl.request('styles:backgrid:entities').toJSON());
					stylesLoader.loadCss(Lvl.request('styles:pace:entities').toJSON());
					stylesLoader.loadCss(Lvl.request('styles:flags:entities').toJSON());
				});
			},
			onClose : function() {
				// don't remove the styles in order to enable them to be reused
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
					placeholder : 'filter sequences'
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
	return Lvl.CollectionApp.Browse.View;
});