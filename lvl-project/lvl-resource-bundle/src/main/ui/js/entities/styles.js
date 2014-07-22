/**
 * RequireJS module that defines the entity: dynamic styles.
 */

define([ 'app' ], function(Lvl) {
    Lvl.module('Entities', function(Entities, Lvl, Backbone, Marionette, $, _) {
        Entities.Style = Backbone.Model.extend({
            defaults : {
                id : 'none',
                url : ''
            },
            validate : function(attrs, options) {
                var errors = {};
                if (!attrs.id) {
                    errors.id = 'can\'t be empty';
                } else {
                    if (attrs.id.length < 2) {
                        errors.id = 'is too short';
                    }
                }
                if (!attrs.url) {
                    errors.url = 'can\'t be empty';
                }
                if (!_.isEmpty(errors)) {
                    return errors;
                }
            }
        });

        Entities.StyleCollection = Backbone.Collection.extend({
            model : Entities.Style,
            comparator : 'id'
        });

        var iniBackgridStyles = function() {
            Entities.backgridStyles = new Entities.StyleCollection([ {
                id : 'backgrid',
                url : '//cdnjs.cloudflare.com/ajax/libs/backgrid.js/0.3.5/backgrid.min.css'
            }, {
                id : 'backgrid-paginator',
                url : '/css/backgrid-paginator.min.css'
            }, {
                id : 'backgrid-select-all',
                url : '/css/backgrid-select-all.min.css'
            }, {
                id : 'backgrid-filter',
                url : '/css/backgrid-filter.min.css'
            } ]);
        };

        var API = {
            getBackgridStyles : function() {
                if (Entities.backgridStyles === undefined) {
                    iniBackgridStyles();
                }
                return Entities.backgridStyles;
            }
        }

        Lvl.reqres.setHandler('styles:backgrid:entities', function() {
            return API.getBackgridStyles();
        });

        return;
    });
});