/**
 * RequireJS entry point.
 */

requirejs.config({
	/* avoids cache. Remove on production! */
	urlArgs : 'bust=' + (new Date()).getTime(),
	baseUrl : 'js',
	paths : {
		/* jQuery JavaScript library */
		'jquery' : [ '//code.jquery.com/jquery-2.1.1.min', 'vendor/cached/jquery.min' ],
		'jquery.toolbar' : 'vendor/provided/jquery.toolbar.min',
		'spin' : [ '//cdnjs.cloudflare.com/ajax/libs/spin.js/2.0.1/spin.min', 'vendor/cached/spin.min' ],
		'jquery.spin' : [ '//cdnjs.cloudflare.com/ajax/libs/spin.js/2.0.1/jquery.spin.min', 'jquery.spin.min' ],
		/* Boostrap front-end framework */
		'bootstrap' : [ '//maxcdn.bootstrapcdn.com/bootstrap/3.2.0/js/bootstrap.min', 'vendor/cached/bootstrap.min' ],
		'bootstrap-switch' : [ '//cdnjs.cloudflare.com/ajax/libs/bootstrap-switch/3.0.2/js/bootstrap-switch.min', 'vendor/cached/bootstrap-switch.min' ],
		'bootstrapvalidator' : [ '//cdnjs.cloudflare.com/ajax/libs/jquery.bootstrapvalidator/0.5.0/js/bootstrapValidator.min',
		                         'vendor/cached/bootstrapValidator.min' ],
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
		                         'backbone.syphon' : [ '//cdnjs.cloudflare.com/ajax/libs/backbone.syphon/0.4.1/backbone.syphon.min', 'vendor/cached/backbone.syphon.min' ],
		                         'backbone.picky' : 'vendor/provided/backbone.picky.min',
		                         'backbone.paginator' : 'vendor/provided/backbone.paginator.min',
		                         'backbone.oauth2' : 'plugins/backbone.oauth2',
		                         /* Data grid based on Backbone */
		                         'backgrid' : [ '//cdnjs.cloudflare.com/ajax/libs/backgrid.js/0.3.5/backgrid.min', 'vendor/cached/backgrid.min' ],
		                         'backgrid-paginator' : 'vendor/provided/backgrid-paginator.min',
		                         'backgrid-select-all' : 'vendor/provided/backgrid-select-all.min',
		                         'backgrid-filter' : 'vendor/provided/backgrid-filter.min',
		                         /* OpenLayers */
		                         'openlayers' : 'vendor/provided/ol',
		                         /* Moment.js */
		                         'moment' : [ '//cdnjs.cloudflare.com/ajax/libs/moment.js/2.7.0/moment.min', 'vendor/cached/moment.min' ],
		                         /* Pace */
		                         'pace' : [ '//cdnjs.cloudflare.com/ajax/libs/pace/0.5.5/pace.min', 'vendor/cached/pace.min' ],
		                         /* qTip2 */
		                         'imagesloaded' : 'vendor/provided/imagesloaded.pkgd.min',
		                         'qtip' : [ '//cdnjs.cloudflare.com/ajax/libs/qtip2/2.2.0/jquery.qtip.min', 'vendor/cached/jquery.qtip.min' ],
		                         /* jsPhyloSVG */
		                         'raphael' : [ '//cdnjs.cloudflare.com/ajax/libs/raphael/2.1.2/raphael-min', 'vendor/cached/raphael-min' ],
		                         'jsphylosvg' : 'vendor/provided/jsphylosvg-min',
		                         /* Add support for underscore templates */
		                         'text' : [ '//cdnjs.cloudflare.com/ajax/libs/require-text/2.0.12/text.min', 'vendor/cached/text.min' ],
		                         'tpl' : [ '//cdnjs.cloudflare.com/ajax/libs/requirejs-tpl/0.0.2/tpl.min', 'vendor/cached/tpl.min' ]
	},
	shim : {
		'jquery.toolbar' : [ 'jquery' ],
		'jquery.spin' : [ 'spin', 'jquery' ],
		'bootstrap' : [ 'jquery' ],
		'flatui-checkbox' : [ 'bootstrap', 'bootstrap-switch', 'jquery.tagsinput', 'jquery.placeholder' ],
		'flatui-radio' : [ 'bootstrap', 'bootstrap-switch', 'jquery.tagsinput', 'jquery.placeholder' ],
		'bootstrapvalidator' : {
			deps : [ 'bootstrap' ],
			exports : 'BootstrapValidator'
		},
		'underscore' : {
			exports : '_'
		},
		'backbone' : {
			deps : [ 'jquery', 'underscore' ],
			exports : 'Backbone'
		},
		'routefilter' : [ 'backbone' ],
		'backbone.syphon' : [ 'backbone' ],
		'backbone.picky' : [ 'backbone' ],
		'backbone.paginator' : [ 'backbone' ],
		'backbone.oauth2' : [ 'backbone' ],
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
		'openlayers' : [ 'jquery' ],
		'moment' : [ 'jquery' ],
		'pace' : [ 'jquery' ],
		'imagesloaded' : [ 'jquery' ],
		'qtip' : [ 'jquery', 'imagesloaded' ],
		'raphael' : {
			exports : 'Raphael'
		},
		'jsphylosvg' : {
			deps : [ 'raphael' ],
			exports : 'Smits'
		},
		'tpl' : [ 'text' ]
	}, callback: function () {
		require([ 'jquery' ], function() {
			// tell jQuery to watch for any 401 or 403 errors and handle them appropriately
			$.ajaxSetup({
				statusCode: {
					401: function() {
						window.location.replace('#login/home/unauthenticated');
					},
					403: function() {
						window.location.replace('#denied');
					}
				}
			});
		});
	}
});

require([ 'app' ], function(Lvl) {
	Lvl.start();
});