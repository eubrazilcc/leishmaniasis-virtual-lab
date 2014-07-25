/**
 * RequireJS entry point.
 */

requirejs.config({
    /* avoids cache. Remove on production! */
    urlArgs : 'bust=' + (new Date()).getTime(),
    baseUrl : 'js',
    paths : {
        /* jQuery JavaScript library */
        'jquery' : [ '//cdnjs.cloudflare.com/ajax/libs/jquery/2.1.1/jquery.min', 'vendor/cached/jquery.min' ],
        'jquery-ui' : [ '//cdnjs.cloudflare.com/ajax/libs/jqueryui/1.10.4/jquery-ui.min', 'vendor/cached/jquery-ui.min' ],
        /* Boostrap front-end framework */
        'jquery.ui.touch-punch' : [ '//cdnjs.cloudflare.com/ajax/libs/jqueryui-touch-punch/0.2.3/jquery.ui.touch-punch.min',
                'vendor/cached/jquery.ui.touch-punch.min' ],
        'bootstrap' : [ '//cdnjs.cloudflare.com/ajax/libs/twitter-bootstrap/3.2.0/js/bootstrap.min', 'vendor/cached/bootstrap.min' ],
        'bootstrap-switch' : [ '//cdnjs.cloudflare.com/ajax/libs/bootstrap-switch/3.0.2/js/bootstrap-switch.min', 'vendor/cached/bootstrap-switch.min' ],
        /* Flat-UI theme (http://designmodo.github.io/Flat-UI/) */
        'flatui-checkbox' : 'vendor/provided/flatui-checkbox',
        'flatui-radio' : 'vendor/provided/flatui-radio',
        'jquery.tagsinput' : [ '//cdnjs.cloudflare.com/ajax/libs/jquery-tagsinput/1.3.3/jquery.tagsinput.min', 'vendor/cached/jquery.tagsinput.min' ],
        'jquery.placeholder' : [ '//cdnjs.cloudflare.com/ajax/libs/jquery-placeholder/2.0.7/jquery.placeholder.min', 'vendor/cached/jquery.placeholder.min' ],
        /* Backbone + Marionette MVC framework */
        'underscore' : [ '//cdnjs.cloudflare.com/ajax/libs/underscore.js/1.6.0/underscore-min', 'vendor/cached/underscore-min' ],
        'backbone' : [ '//cdnjs.cloudflare.com/ajax/libs/backbone.js/1.1.2/backbone-min', 'vendor/cached/backbone-min' ],
        'marionette' : [ '//cdnjs.cloudflare.com/ajax/libs/backbone.marionette/1.8.8/backbone.marionette.min', 'vendor/cached/backbone.marionette.min' ],
        /* Useful backbone plug-ins */
        'routefilter' : [ '//cdnjs.cloudflare.com/ajax/libs/backbone.routefilter/0.2.0/backbone.routefilter.min', 'vendor/cached/backbone.routefilter.min' ],
        'backbone.picky' : 'vendor/provided/backbone.picky.min',
        'backbone.paginator' : 'vendor/provided/backbone.paginator.min',
        /* Data grid based on Backbone */
        'backgrid' : [ '//cdnjs.cloudflare.com/ajax/libs/backgrid.js/0.3.5/backgrid.min', 'vendor/cached/backgrid.min' ],
        'backgrid-paginator' : 'vendor/provided/backgrid-paginator.min',
        'backgrid-select-all' : 'vendor/provided/backgrid-select-all.min',
        'backgrid-filter' : 'vendor/provided/backgrid-filter.min',
        /* OpenLayers */
        'openlayers' : 'vendor/provided/ol',
        /* Add support for underscore templates */
        'text' : [ '//cdnjs.cloudflare.com/ajax/libs/require-text/2.0.12/text.min', 'vendor/cached/text.min' ],
        'tpl' : [ '//cdnjs.cloudflare.com/ajax/libs/requirejs-tpl/0.0.2/tpl.min', 'vendor/cached/tpl.min' ]
    },
    shim : {
        'jquery-ui' : [ 'jquery' ],
        'jquery.ui.touch-punch' : [ 'jquery', 'jquery-ui' ],
        'bootstrap' : [ 'jquery', 'jquery-ui', 'jquery.ui.touch-punch' ],
        'flatui-checkbox' : [ 'bootstrap', 'bootstrap-switch', 'jquery.tagsinput', 'jquery.placeholder' ],
        'flatui-radio' : [ 'bootstrap', 'bootstrap-switch', 'jquery.tagsinput', 'jquery.placeholder' ],
        'underscore' : {
            exports : '_'
        },
        'backbone' : {
            deps : [ 'jquery', 'underscore' ],
            exports : 'Backbone'
        },
        'routefilter' : [ 'backbone' ],
        'backbone.picky' : [ 'backbone' ],
        'backbone.paginator' : [ 'backbone' ],
        'backgrid' : {
            deps : [ 'backbone' ],
            exports : 'Backgrid'
        },
        'backgrid-paginator' : [ 'backgrid', 'backbone.paginator' ],
        'backgrid-select-all' : [ 'backgrid' ],
        'backgrid-filter' : [ 'backgrid' ],
        'marionette' : {
            deps : [ 'backbone' ],
            exports : 'Marionette'
        },
        'openlayers' : {
            deps : [ 'jquery' ],
            exports : 'ol'
        },
        'tpl' : [ 'text' ]
    }
});

require([ 'app' ], function(Lvl) {
    Lvl.start();
});