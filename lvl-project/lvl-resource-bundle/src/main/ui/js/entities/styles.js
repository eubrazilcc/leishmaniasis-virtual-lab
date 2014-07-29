/**
 * RequireJS module that defines the entity: dynamic styles. Note that this
 * module uses the 'requirejs.s.contexts._.config' hack to read values of
 * configuration that could change or disappear in the next versions of
 * RequireJS without warning.
 */

define([ 'app' ], function(Lvl) {
    var bust = requirejs.s.contexts._.config.urlArgs ? '?' + requirejs.s.contexts._.config.urlArgs : '';
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

        var iniFormValidationStyles = function() {
            Entities.formValidationStyles = new Entities.StyleCollection([ {
                id : 'bootstrapvalidator',
                url : '//cdnjs.cloudflare.com/ajax/libs/jquery.bootstrapvalidator/0.5.0/css/bootstrapValidator.min.css' + bust
            } ]);
        };

        var iniBackgridStyles = function() {
            Entities.backgridStyles = new Entities.StyleCollection([ {
                id : 'backgrid',
                url : '//cdnjs.cloudflare.com/ajax/libs/backgrid.js/0.3.5/backgrid.min.css' + bust
            }, {
                id : 'backgrid-paginator',
                url : '/css/backgrid-paginator.min.css' + bust
            }, {
                id : 'backgrid-select-all',
                url : '/css/backgrid-select-all.min.css' + bust
            }, {
                id : 'backgrid-filter',
                url : '/css/backgrid-filter.min.css' + bust
            } ]);
        };

        var iniOpenLayersStyles = function() {
            Entities.openLayersStyles = new Entities.StyleCollection([ {
                id : 'ol',
                url : '/css/ol.css' + bust
            } ]);
        };

        var iniJQueryToolbarStyles = function() {
            Entities.jQueryToolbarStyles = new Entities.StyleCollection([ {
                id : 'jquery.toolbar',
                url : '/css/jquery.toolbars.css' + bust
            } ]);
        };

        var API = {
            getFormValidationStyles : function() {
                if (Entities.formValidationStyles === undefined) {
                    iniFormValidationStyles();
                }
                return Entities.formValidationStyles;
            },
            getBackgridStyles : function() {
                if (Entities.backgridStyles === undefined) {
                    iniBackgridStyles();
                }
                return Entities.backgridStyles;
            },
            getOpenLayersStyles : function() {
                if (Entities.openLayersStyles === undefined) {
                    iniOpenLayersStyles();
                }
                return Entities.openLayersStyles;
            },
            getJQueryToolbarStyles : function() {
                if (Entities.jQueryToolbarStyles === undefined) {
                    iniJQueryToolbarStyles();
                }
                return Entities.jQueryToolbarStyles;
            }
        }

        Lvl.reqres.setHandler('styles:form-validation:entities', function() {
            return API.getFormValidationStyles();
        });

        Lvl.reqres.setHandler('styles:backgrid:entities', function() {
            return API.getBackgridStyles();
        });

        Lvl.reqres.setHandler('styles:openlayers:entities', function() {
            return API.getOpenLayersStyles();
        });

        Lvl.reqres.setHandler('styles:jquery.toolbar:entities', function() {
            return API.getJQueryToolbarStyles();
        });

        return;
    });
});