/**
 * RequireJS module that defines the view: collection->browse.
 */

define([ 'app', 'tpl!apps/collection/browse/templates/collection_browse', 'apps/config/marionette/styles/style', 'flatui-checkbox', 'flatui-radio', 'backgrid',
        'backgrid-paginator', 'backgrid-select-all', 'backgrid-filter' ], function(Lvl, BrowseTpl, Style) {
    Lvl.module('CollectionApp.Browse.View', function(View, Lvl, Backbone, Marionette, $, _) {

        // TODO : controller
        var Sequence = Backbone.Model.extend({});
        var Sequences = Backbone.PageableCollection.extend({
            model : Sequence,
            url : 'sequences.json',
            state : {
                pageSize : 25
            },
            mode : 'client'
        });

        var sequences = new Sequences();
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
        }, /*
             * { name : 'version', label : 'Version', editable : false, cell :
             * 'string' }, { name : 'gi', label : 'GI', editable : false, cell :
             * 'string' },
             */{
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
            collection : sequences
        });

        View.Content = Marionette.ItemView.extend({
            id : 'browse',
            template : BrowseTpl,
            onBeforeRender : function() {
                require([ 'entities/styles' ], function() {
                    new Style().loadCss(Lvl.request('styles:backgrid:entities').toJSON());
                });
            },
            onClose : function() {
                /* don't remove the styles in order to enable them to be reused */
            },
            onRender : function() {
                var gridContainer = this.$('#grid-container');
                gridContainer.append(grid.render().el);

                var paginator = new Backgrid.Extension.Paginator({
                    collection : sequences
                });

                gridContainer.after(paginator.render().el);

                var filter = new Backgrid.Extension.ClientSideFilter({
                    collection : sequences,
                    fields : [ 'definition' ],
                    placeholder: 'filter by definition'
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