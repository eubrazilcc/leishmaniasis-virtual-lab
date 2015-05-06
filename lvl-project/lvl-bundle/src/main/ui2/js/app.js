/**
 * RequireJS module that defines the LVL main application.
 */

define([ 'marionette', 'apps/config/marionette/regions/dialog' ], function(Marionette) {
	'use strict';
	var Lvl = new Marionette.Application();

	require([ 'domReady' ], function(domReady) {
		domReady(function() {
			// browser window scroll (in pixels) after which the "back to top"
			// link is shown
			var offset = 300,
			// browser window scroll (in pixels) after which the "back to top"
			// link opacity is reduced
			offset_opacity = 1200,
			// duration of the top scrolling animation (in ms)
			scroll_top_duration = 700,
			// grab the "back to top" link
			$back_to_top = $('.lvl-back-to-top');
			// hide or show the "back to top" link
			$(window).scroll(function() {
				$back_to_top.removeAttr('style');
				($(this).scrollTop() > offset) ? $back_to_top.addClass('btt-is-visible') : $back_to_top.removeClass('btt-is-visible btt-fade-out');
				if ($(this).scrollTop() > offset_opacity) {
					$back_to_top.addClass('btt-fade-out');
				}
			});
			// smooth scroll to top
			$back_to_top.on('click', function(event) {
				event.preventDefault();
				$('body,html').animate({
					scrollTop : 0,
				}, scroll_top_duration);
			});
		});
	});

	Lvl.addRegions({
		headerRegion : '#header-region',
		mainRegion : '#main-region',
		dialogRegion : Marionette.Region.Dialog.extend({
			el : '#dialog-region'
		}),
		fullpageRegion : '#fullpage-region',
		footerRegion : '#footer-region'
	});

	var flash = {
		params : {},
		addParams : function(options) {
			this.params = options || {};
		},
		reset : function() {
			this.params = {};
		}
	};

	Lvl.flash = function(params) {
		flash.addParams(params);
		return this;
	};

	Lvl.flashed = function() {
		var params = flash.params;
		flash.reset();		
		return params;
	};

	Lvl.navigate = function(route, options) {
		options || (options = {});
		Backbone.history.navigate(route, options);
	};

	Lvl.getCurrentRoute = function() {
		return Backbone.history.fragment
	};

	Lvl.vent = new Backbone.Wreqr.EventAggregator();

	Lvl.startSubApp = function(appName, args) {
		var currentApp = appName ? Lvl.module(appName) : null;
		if (Lvl.currentApp === currentApp) {
			return;
		}
		if (Lvl.currentApp) {
			Lvl.currentApp.stop();
		}
		Lvl.currentApp = currentApp;
		if (currentApp) {
			currentApp.start(args);
		}
	};

	var History = Backbone.History.extend({
		loadUrl : function() {
			var match = Backbone.History.prototype.loadUrl.apply(this, arguments);
			if (!match) {
				if (Lvl.getCurrentRoute() !== '') {
					require([ 'apps/not-found/not-found_app' ], function() {
						Lvl.execute('set:active:header', 'home');
						Lvl.execute('set:active:footer', 'home');
						Lvl.startSubApp('NotFoundApp');
						Lvl.execute('show:not_found');
					});
				}
			}
			return match;
		}
	});

	Lvl.on('start', function() {
		if (Backbone.history) {
			Backbone.history instanceof History || (Backbone.history = new History());
			require([ 'apps/config/marionette/styles/style', 'apps/header/header_app', 'apps/footer/footer_app', 'apps/home/home_router',
					'apps/access/access_router', 'apps/collection/collection_router', 'apps/social/social_router', 'apps/e-compendium/e-compendium_router',
					'apps/analysis/analysis_router', 'apps/enm/enm_router', 'apps/drive/drive_router', 'apps/saved-items/saved-items_router',
					'apps/settings/settings_router', 'apps/open/about/about_router', 'apps/open/documentation/documentation_router',
					'apps/open/support/support_router', 'apps/open/software/software_router' ], function(Style) {
				// load base styles
				new Style().loadBaseStyles();
				// start history
				Backbone.history.start();
				// only if the initial call was done from the root folder, load
				// the default route
				if (Lvl.getCurrentRoute() === '') {
					Lvl.navigate('home', {
						trigger : true
					});
				}
			});
		}
	});

	return Lvl;
});