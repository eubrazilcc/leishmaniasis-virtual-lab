/**
 * RequireJS module that defines the view: drive->datasets.
 */

define([ 'app', 'marionette', 'tpl!apps/drive/datasets/templates/drive_datasets', 'apps/config/marionette/styles/style',
		'apps/config/marionette/configuration', 'pace', 'moment', 'filesize', 'backbone.oauth2', 'backgrid', 'backgrid-paginator', 'backgrid-select-all',
		'backgrid-filter' ], function(Lvl, Marionette, DatasetsTpl, Style, Configuration, pace, moment, filesize) {
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
								this.$el.append('<a href="' + config.get('service', '') + '/datasets/objects/~/' + formattedValue + "/download?"
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
				'click a[data-dataset]' : 'createLink',
				'click a[data-remove]' : 'removeDataset'
			},
			deselectAll : function(e) {
				e.preventDefault();
				this.grid.clearSelectedModels();
			},
			createLink : function(e) {
				e.preventDefault();
				var self = this;
				var target = $(e.target);
				var itemId = target.is('i') ? target.parent('a').get(0).getAttribute('data-dataset') : target.getAttribute('data-dataset');
				require([ 'common/confirm', 'entities/link' ], function(confirmDialog, LinkModel) {
					confirmDialog('Confirm public link creation', 'Create a public link to the dataset: ' + itemId + '?', function() {
						var newLink = new LinkModel.LinkCreate();
						newLink.oauth2_token = config.authorizationToken();
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
				item.oauth2_token = config.authorizationToken();
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