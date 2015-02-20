/**
 * RequireJS module that defines the view: drive->links.
 */

define([ 'app', 'marionette', 'tpl!apps/drive/links/templates/drive_links', 'apps/config/marionette/styles/style', 'apps/config/marionette/configuration',
		'pace', 'moment', 'backbone.oauth2', 'backgrid', 'backgrid-paginator', 'backgrid-select-all', 'backgrid-filter' ], function(Lvl, Marionette, LinksTpl,
		Style, Configuration, pace, moment) {
	Lvl.module('DriveApp.Links.View', function(View, Lvl, Backbone, Marionette, $, _) {
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
					cell : Backgrid.Cell.extend({
						render : function() {
							this.$el.empty();
							var rawValue = this.model.get(this.column.get('name'));
							var formattedValue = this.formatter.fromRaw(rawValue, this.model);
							if (formattedValue && typeof formattedValue === 'string') {
								this.$el.append('<a href="' + config.get('service', '') + '/public/datasets/' + formattedValue
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
				'click a#uncheck-btn' : 'deselectAll',
				'click a[data-remove]' : 'removeLink'
			},
			deselectAll : function(e) {
				e.preventDefault();
				this.grid.clearSelectedModels();
			},
			removeLink : function(e) {
				e.preventDefault();
				var self = this;
				var target = $(e.target);
				var itemId = target.is('i') ? target.parent('a').get(0).getAttribute('data-remove') : target.attr('data-remove');
				var item = this.collection.get(itemId);
				item.oauth2_token = config.authorizationToken();
				this.collection.remove(item);
				item.destroy({
					success : function(e) {
					},
					error : function(e) {
						require([ 'qtip' ], function(qtip) {
							var message = $('<p />', {
								text : 'The public link cannot be removed.'
							}), ok = $('<button />', {
								text : 'Close',
								'class' : 'full'
							});
							$('#alert').qtip({
								content : {
									text : message.add(ok),
									title : {
										text : 'Error',
										button : true
									}
								},
								position : {
									my : 'center',
									at : 'center',
									target : $(window)
								},
								show : {
									ready : true,
									modal : {
										on : true,
										blur : false
									}
								},
								hide : false,
								style : 'qtip-bootstrap dialogue',
								events : {
									render : function(event, api) {
										$('button', api.elements.content).click(function() {
											api.hide();
										});
									},
									hide : function(event, api) {
										self.grid.insertRow([ item ]);
										api.destroy();
									}
								}
							});
						});
					}
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
					placeholder : 'filter links'
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
	return Lvl.DriveApp.Links.View;
});