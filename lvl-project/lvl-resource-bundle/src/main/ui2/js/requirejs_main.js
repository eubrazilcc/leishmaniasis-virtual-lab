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
		'backbone.picky' : 'vendor/provided/backbone.picky.min',
		/* Moment.js */
		'moment' : [ '//cdnjs.cloudflare.com/ajax/libs/moment.js/2.9.0/moment.min', 'vendor/cached/moment.min' ],
		/* qTip2 */
		'imagesloaded' : [ '//cdnjs.cloudflare.com/ajax/libs/jquery.imagesloaded/3.1.8/imagesloaded.pkgd.min', 'imagesloaded.pkgd.min' ],
		'qtip' : [ '//cdnjs.cloudflare.com/ajax/libs/qtip2/2.2.0/jquery.qtip.min', 'vendor/cached/jquery.qtip.min' ], // 2.2.1: map is unavailable
		/* Add support for underscore templates */
		'text' : [ '//cdnjs.cloudflare.com/ajax/libs/require-text/2.0.12/text.min', 'vendor/cached/text.min' ],
		'tpl' : [ '//cdnjs.cloudflare.com/ajax/libs/requirejs-tpl/0.0.2/tpl.min', 'vendor/cached/tpl.min' ]
	},
	shim : {
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
		'backbone.picky' : {
			deps : [ 'backbone' ]
		},
		'marionette' : {
			deps : [ 'backbone' ],
			exports : 'Marionette'
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