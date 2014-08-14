/**
 * RequireJS module that defines the view: collection->browse.
 */

define([ 'app', 'tpl!apps/collection/browse/templates/collection_browse', 'apps/config/marionette/styles/style', 'apps/config/marionette/configuration',
        'pace', 'common/country_names', 'backbone.oauth2', 'flatui-checkbox', 'flatui-radio', 'backgrid', 'backgrid-paginator', 'backgrid-select-all',
        'backgrid-filter' ], function(Lvl, BrowseTpl, Style, Configuration, pace, mapCn) {
    Lvl.module('CollectionApp.Browse.View', function(View, Lvl, Backbone, Marionette, $, _) {
        var config = new Configuration();

        // TODO : controller
        var Sequence = Backbone.Model.extend({});
        var Sequences = Backbone.PageableCollection.extend({
            model : Sequence,
            mode : 'server',
            // TODO url : 'sequences.json?burst=' + Math.random(),
            url : config.get('service', '') + '/sequences',
            state : {
                pageSize : 100,
                firstPage : 0
            },
            queryParams : {
                totalPages : null,
                totalRecords : null,
                currentPage : 'page',
                pageSize : 'per_page',
                sortKey : null
            },
            parseState : function(resp, queryParams, state, options) {
                return {
                    totalRecords : resp.totalCount
                };
            },
            parseRecords : function(resp, options) {
                return resp.sequences;
            }
        });

        var sequences = new Sequences();
        sequences.oauth2_token = config.authorizationToken();
        // TODO : controller

        // TODO Showing {{sequences.length}} sequences
        
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
                } ];
        var grid = new Backgrid.Grid({
            columns : [ {
                name : '',
                cell : 'select-row',
                headerCell : 'select-all'
            } ].concat(columns),
            collection : sequences,
            emptyText : 'No sequences found'
        });

        View.Content = Marionette.ItemView.extend({
            id : 'browse',
            template : BrowseTpl,
            initialize : function() {
                this.listenTo(sequences, 'request', this.displaySpinner);
                this.listenTo(sequences, 'sync error', this.removeSpinner);
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
            },
            onRender : function() {
                var self = this;
                pace.start();

                var gridContainer = this.$('#grid-container');
                gridContainer.append(grid.render().el);

                var paginator = new Backgrid.Extension.Paginator({
                    collection : sequences,
                    windowSize : 14,
                    slideScale : 0.5,
                    goBackFirstOnSort : false
                });

                gridContainer.after(paginator.render().el);

                $(paginator.el).css({
                    'margin-top' : '20px'
                });

                var filter = new Backgrid.Extension.ServerSideFilter({
                    collection : sequences,
                    name : 'q',
                    placeholder : 'filter sequences'
                });

                gridContainer.before(filter.render().el);
                
                $(filter.el).addClass('pull-right lvl-filter-container');

                // TODO : controller
                sequences.fetch({
                    reset : true
                });
                // TODO : controller
            }
        });
    });
    return Lvl.CollectionApp.Browse.View;
});