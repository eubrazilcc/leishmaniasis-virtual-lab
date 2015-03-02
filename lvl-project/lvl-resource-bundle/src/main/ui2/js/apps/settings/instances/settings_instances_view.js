/**
 * RequireJS module that defines the view: settings->instances.
 */

define([ 'app', 'tpl!apps/settings/instances/templates/settings_instances', 'apps/config/marionette/styles/style', 'entities/reference', 'pace', 'moment',
		'common/country_names', 'backbone.oauth2', 'backgrid', 'backgrid-paginator', 'backgrid-select-all', 'backgrid-filter' ], function(Lvl, InstanceTpl,
		Style, SequenceModel, pace, moment, mapCn) {
	Lvl.module('SettingsApp.Instance.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var columns = [ {
			name : 'instanceId',
			label : 'Id',
			editable : false,
			cell : 'string'
		}, {
			name : 'roles',
			label : 'Roles',
			editable : false,
			cell : Backgrid.Cell.extend({
				render : function() {
					this.$el.empty();
					var rawValue = this.model.get(this.column.get('name'));
					if (rawValue !== undefined) {
						var names = '';
						for (var i = 0; i < rawValue.length; i++) {
							var color = 'label-default';
							if ('broker' === rawValue[i]) {
								color = 'label-primary';
							} else if ('shard' === rawValue[i]) {
								color = 'label-info';
							} else if ('auth' === rawValue[i]) {
								color = 'label-warning';
							}
							names += '<span class="label ' + color + '">' + rawValue[i] + '</span> ';
						}
						this.$el.append(names.trim());
					}
					this.delegateEvents();
					return this;
				}
			})
		}, {
			name : 'heartbeat',
			label : 'Heartbeat',
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
		}, {
			name : 'location',
			label : 'Location',
			editable : false,
			cell : Backgrid.Cell.extend({
				render : function() {
					this.$el.empty();
					var rawValue = this.model.get(this.column.get('name'));
					if (rawValue !== undefined && rawValue.coordinates !== undefined) {
						var longitude = rawValue.coordinates[0];
						var latitude = rawValue.coordinates[1];
						if (longitude && typeof longitude === 'number' && latitude && typeof latitude === 'number') {
							this.$el.append('lon:<mark>' + longitude + '</mark>, lat:<mark>' + latitude + '</mark>');
						}
					}					
					this.delegateEvents();
					return this;
				}
			})
		} ];
		View.Content = Marionette.ItemView.extend({
			id : 'instances',
			template : InstanceTpl,
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
					emptyText : 'No instances found'
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
			},
			deselectAll : function(e) {
				e.preventDefault();
				this.grid.clearSelectedModels();
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
					placeholder : 'filter instances'
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
	return Lvl.SettingsApp.Instance.View;
});