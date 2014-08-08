/**
 * RequireJS module that defines the view: collection->browse.
 */

define([ 'app', 'tpl!apps/collection/browse/templates/collection_browse', 'apps/config/marionette/styles/style', 'apps/config/marionette/configuration',
        'pace', 'flatui-checkbox', 'flatui-radio', 'backgrid', 'backgrid-paginator', 'backgrid-select-all', 'backgrid-filter', 'backbone.oauth2' ], function(
        Lvl, BrowseTpl, Style, Configuration, pace) {
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
                currentPage : 'start',
                pageSize : 'size',
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

        var columns = [ {
            name : 'dataSource',
            label : 'Source',
            editable : false,
            cell : 'string'
        }, {
            name : 'definition',
            label : 'Definition',
            editable : false,
            cell : 'string'
        }, {
            name : 'accession',
            label : 'Accession',
            editable : false,
            cell : 'string'
        }, {
            name : 'organism',
            label : 'Organism',
            editable : false,
            cell : 'string'
        }, {
            name : 'countryFeature',
            label : 'Country',
            editable : false,
            cell : 'string'
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
            },
            onBeforeRender : function() {
                require([ 'entities/styles' ], function() {
                    var stylesLoader = new Style();
                    stylesLoader.loadCss(Lvl.request('styles:backgrid:entities').toJSON());
                    stylesLoader.loadCss(Lvl.request('styles:pace:entities').toJSON());
                });
            },
            onClose : function() {
                /* don't remove the styles in order to enable them to be reused */
            },
            onRender : function() {
                // pace.start();

                var gridContainer = this.$('#grid-container');
                gridContainer.append(grid.render().el);

                var paginator = new Backgrid.Extension.Paginator({
                    collection : sequences
                });

                gridContainer.after(paginator.render().el);

                var filter = new Backgrid.Extension.ClientSideFilter({
                    collection : sequences,
                    fields : [ 'definition' ],
                    placeholder : 'filter by definition'
                });

                gridContainer.before(filter.render().el);

                $(filter.el).css({
                    float : 'right',
                    margin : '10px 20px 20px 20px',
                    padding : '0px'
                });

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