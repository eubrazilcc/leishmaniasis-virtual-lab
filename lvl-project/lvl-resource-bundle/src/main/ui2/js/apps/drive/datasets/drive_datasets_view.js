/**
 * RequireJS module that defines the view: drive->datasets.
 */

define(
		[ 'app', 'marionette', 'tpl!apps/drive/datasets/templates/drive_datasets', 'apps/config/marionette/styles/style',
				'apps/config/marionette/configuration', 'pace', 'moment', 'backbone.oauth2', 'backgrid', 'backgrid-paginator', 'backgrid-select-all',
				'backgrid-filter' ], function(Lvl, Marionette, DatasetsTpl, Style, Configuration, pace, moment) {
			Lvl.module('DriveApp.Datasets.View', function(View, Lvl, Backbone, Marionette, $, _) {
				'use strict';
				var config = new Configuration();
				var columns = [
						{
							name : 'filename',
							label : 'Name',
							editable : false,
							cell : 'string'
						},
						{
							name : 'length',
							label : 'Length',
							editable : false,
							cell : 'integer'
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
										

										/* TODO this.$el.append('<a href="' + config.get('service', '') + '/datasets/objects/files/' + formattedValue + "/download?"
												+ config.authorizationQuery()
												+ '" target="_blank" title="Share" class="text-muted"><i class="fa fa-share fa-fw"></i></a>'); */
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
										this.$el.append('<a href="' + config.get('service', '') + '/datasets/objects/files/' + formattedValue + "/download?"
												+ config.authorizationQuery()
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
						'click a[data-remove]' : 'removeDataset'
					},
					deselectAll : function(e) {
						e.preventDefault();
						this.grid.clearSelectedModels();
					},
					removeDataset : function(e) {
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
										text : 'The dataset cannot be removed.'
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
							placeholder : 'filter datasets'
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
			return Lvl.DriveApp.Datasets.View;
		});