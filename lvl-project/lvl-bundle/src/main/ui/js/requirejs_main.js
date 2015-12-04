/**
 * RequireJS entry point.
 */

requirejs.config({
	/* avoids cache. Remove on production! */
	urlArgs : 'bust=' + (new Date()).getTime(),
	baseUrl : 'js',
	paths : {
		/* jQuery JavaScript library */
		'jquery' : '//code.jquery.com/jquery-2.1.4.min', // 2.1.4
		'spin' : 'vendor/spin.min', // 2.1.0
		'jquery.spin' : 'vendor/jquery.spin.min', // 2.3.2
		/* Boostrap front-end framework */
		'bootstrap' : '//maxcdn.bootstrapcdn.com/bootstrap/3.3.6/js/bootstrap.min', // 3.3.6
		'bootstrapvalidator' : 'vendor/bootstrapValidator.min', // 0.5.3 (0.6.2 is commercial)
		'bootstrap3-typeahead' : 'vendor/bootstrap3-typeahead.min', // 3.1.0
		/* Backbone + Marionette MVC framework */
		'underscore' : 'vendor/underscore-min', // 1.8.3
		'backbone' : 'vendor/backbone-min', // 1.2.3
		'marionette' : 'vendor/backbone.marionette.min', // 2.4.3
		/* Useful backbone plug-ins */
		'routefilter' : 'vendor/backbone.routefilter.min', // 0.2.0
		'backbone.syphon' : 'vendor/backbone.syphon.min', // 0.6.3
		'backbone.picky' : 'vendor/backbone.picky.min', // 0.2.0
		'backbone.paginator' : 'vendor/backbone.paginator.min', // 2.0.0
		'backbone.oauth2' : 'plugins/backbone.oauth2',
		/* Data grid based on Backbone */
		'backgrid' : 'vendor/backgrid.min', // 0.3.5
		'backgrid-paginator' : 'vendor/backgrid-paginator.min', // 0.3.5
		'backgrid-select-all' : 'vendor/backgrid-select-all.min', // 0.3.5
		'backgrid-filter' : 'vendor/backgrid-filter.min', // 0.3.5
		/* OpenLayers */
		'openlayers' : 'vendor/ol.min', // 3.11.1
		/* Filesize.js */
		'filesize' : 'vendor/filesize.min', // 3.1.2
		/* FileSaver.js */
		'FileSaver' : 'vendor/FileSaver.min', // 1.1.20151003
		/* Chance.js */
		'chance' : 'vendor/chance.min', // 0.8.0
		/* Moment.js */
		'moment' : 'vendor/moment.min', // 2.10.6
		/* Pace */
		'pace' : 'vendor/pace.min', // 1.0.0
		/* qTip2 */
		'imagesloaded' : 'vendor/imagesloaded.pkgd.min', // 3.2.0
		'qtip' : 'vendor/jquery.qtip.min', // 2.2.1
		/* Chart.js */
		'chartjs' : 'vendor/Chart.min', // 1.0.2
		/* Hopscotch */
		'hopscotch' : 'vendor/hopscotch.min', // 0.2.5
		/* SVG pan and zoom */
		'jquery.panzoom' : 'vendor/jquery.panzoom.min', // 2.0.5
		/* Add support for underscore templates */
		'text' : 'vendor/text.min', // 2.0.14
		'tpl' : 'vendor/tpl.min', // 0.0.2
		/* Wait for the DOM is ready */
		'domReady' : 'vendor/domReady.min' // 2.0.1
	},
	shim : {
		'bootstrap' : {
			deps : [ 'jquery' ]
		},
		'bootstrapvalidator' : {
			deps : [ 'bootstrap' ]
		},
		'backgrid' : {
			deps : [ 'backbone' ],
			exports : 'Backgrid'
		},
		'backgrid-select-all' : {
			deps : [ 'backgrid' ]
		},
		'backgrid-filter' : {
			deps : [ 'backgrid' ]
		},
		'FileSaver' : {
			exports : 'saveAs'
		}
	},
	callback : function() {
		require([ 'jquery' ], function() {
			// tell jQuery to watch for any 401, 403 or 404 errors and handle them appropriately
			$.ajaxSetup({
				cache : false,
				statusCode : {
					401 : function() {
						window.location.replace('#login/home/unauthenticated');
					},
					403 : function() {
						window.location.replace('#denied');
					},
					404 : function() {
						window.location.replace('#not-found');
					}
				}
			});
		});
	},
	waitSeconds : 7
});

require([ 'app', 'jquery', 'bootstrap' ], function(Lvl) {
	// load configuration properties, and then start the LVL application
	Lvl.config.loadProperties().then(Lvl.start());
});