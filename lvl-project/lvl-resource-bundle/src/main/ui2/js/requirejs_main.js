/**
 * RequireJS entry point.
 */

requirejs.config({
	/* avoids cache. Remove on production! */
	urlArgs : 'bust=' + (new Date()).getTime(),
	baseUrl : 'js',
	paths : {
		/* jQuery JavaScript library */
		'jquery' : [ '//code.jquery.com/jquery-2.1.3.min', 'vendor/cached/jquery.min' ],
		'spin' : [ '//cdnjs.cloudflare.com/ajax/libs/spin.js/2.0.1/spin.min', 'vendor/cached/spin.min' ],
		'jquery.spin' : [ '//cdnjs.cloudflare.com/ajax/libs/spin.js/2.0.1/jquery.spin.min', 'jquery.spin.min' ],
		/* Boostrap front-end framework */
		'bootstrap' : [ '//maxcdn.bootstrapcdn.com/bootstrap/3.3.2/js/bootstrap.min', 'vendor/cached/bootstrap.min' ],
		'bootstrapvalidator' : [ '//cdnjs.cloudflare.com/ajax/libs/jquery.bootstrapvalidator/0.5.3/js/bootstrapValidator.min', // 0.5.4: commercial license
		'vendor/cached/bootstrapValidator.min' ],
		/* Backbone + Marionette MVC framework */
		'underscore' : [ '//cdnjs.cloudflare.com/ajax/libs/underscore.js/1.6.0/underscore-min', 'vendor/cached/underscore-min' ], // 1.7.0: incompatible '_.extend'
		'backbone' : [ '//cdnjs.cloudflare.com/ajax/libs/backbone.js/1.1.2/backbone-min', 'vendor/cached/backbone-min' ],
		'marionette' : [ '//cdnjs.cloudflare.com/ajax/libs/backbone.marionette/2.3.2/backbone.marionette.min', 'vendor/cached/backbone.marionette.min' ],
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
		'openlayers' : [ '//cdnjs.cloudflare.com/ajax/libs/ol3/3.2.1/ol.min', 'vendor/cached/ol.min' ],
		/* Filesize.js */
		'filesize' : [ '//cdn.filesizejs.com/filesize.min', 'vendor/cached/filesize.min' ],
		/* Chance.js */
		'chance' : 'vendor/provided/chance.min',
		/* Moment.js */
		'moment' : [ '//cdnjs.cloudflare.com/ajax/libs/moment.js/2.9.0/moment.min', 'vendor/cached/moment.min' ],
		/* Pace */
		'pace' : [ '//cdnjs.cloudflare.com/ajax/libs/pace/1.0.2/pace.min', 'vendor/cached/pace.min' ],
		/* qTip2 */
		'imagesloaded' : [ '//cdnjs.cloudflare.com/ajax/libs/jquery.imagesloaded/3.1.8/imagesloaded.pkgd.min', 'vendor/cached/imagesloaded.pkgd.min' ],
		'qtip' : [ '//cdnjs.cloudflare.com/ajax/libs/qtip2/2.2.0/jquery.qtip.min', 'vendor/cached/jquery.qtip.min' ], // 2.2.1: map is unavailable
		/* jsPhyloSVG */
		'raphael' : [ '//cdnjs.cloudflare.com/ajax/libs/raphael/2.1.2/raphael-min', 'vendor/cached/raphael-min' ],
		'jsphylosvg' : 'vendor/provided/jsphylosvg-min',
		/* Chart.js */
		'chartjs' : [ '//cdnjs.cloudflare.com/ajax/libs/Chart.js/1.0.1/Chart.min', 'vendor/cached/Chart.min' ],
		/* Add support for underscore templates */
		'text' : [ '//cdnjs.cloudflare.com/ajax/libs/require-text/2.0.12/text.min', 'vendor/cached/text.min' ],
		'tpl' : [ '//cdnjs.cloudflare.com/ajax/libs/requirejs-tpl/0.0.2/tpl.min', 'vendor/cached/tpl.min' ]
	},
	shim : {
		'jquery.spin' : {
			deps : [ 'spin', 'jquery' ]
		},
		'bootstrap' : {
			deps : [ 'jquery' ]
		},
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
		'routefilter' : {
			deps : [ 'backbone' ]
		},
		'backbone.syphon' : {
			deps : [ 'backbone' ]
		},
		'backbone.picky' : {
			deps : [ 'backbone' ]
		},
		'backbone.paginator' : {
			deps : [ 'backbone' ]
		},
		'backbone.oauth2' : {
			deps : [ 'backbone' ]
		},
		'backgrid' : {
			deps : [ 'backbone' ],
			exports : 'Backgrid'
		},
		'backgrid-paginator' : {
			deps : [ 'backgrid', 'backbone.paginator' ]
		},
		'backgrid-select-all' : {
			deps : [ 'backgrid' ]
		},
		'backgrid-filter' : {
			deps : [ 'backgrid' ]
		},
		'marionette' : {
			deps : [ 'backbone' ],
			exports : 'Marionette'
		},
		'openlayers' : {
			deps : [ 'jquery' ],
			exports : 'ol'
		},
		'chance' : {
			exports : 'Chance'
		},
		'moment' : {
			deps : [ 'jquery' ]
		},
		'imagesloaded' : {
			deps : [ 'jquery' ]
		},
		'qtip' : {
			deps : [ 'jquery', 'imagesloaded' ]
		},
		'raphael' : {
			exports : 'Raphael'
		},
		'jsphylosvg' : {
			deps : [ 'raphael' ],
			exports : 'Smits'
		},
		'chartjs'  : {
			exports : 'Chart'
		},
		'tpl' : {
			deps : [ 'text' ]
		}
	},
	callback : function() {
		require([ 'jquery' ], function() {
			// tell jQuery to watch for any 401 or 403 errors and handle them
			// appropriately
			$.ajaxSetup({
				statusCode : {
					401 : function() {
						window.location.replace('#login/home/unauthenticated');
					},
					403 : function() {
						window.location.replace('#denied');
					}
				}
			});
		});
	},
	waitSeconds : 7
});

require([ 'app', 'jquery', 'bootstrap' ], function(Lvl) {
	Lvl.start();
});