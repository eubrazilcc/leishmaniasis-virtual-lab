/**
 * RequireJS module that defines the controller: open->about->show.
 */

define([ 'app', 'apps/open/layout/open_layout_ctrl', 'tpl!apps/open/about/show/tpls/about-main-section' ], function(Lvl, LayoutController, MainSectionTpl) {
	Lvl.module('AboutApp.Show', function(Show, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Show.MainSectionView = Marionette.ItemView.extend({
			template : MainSectionTpl
		});
		Show.Controller = {
			showAbout : function(section) {
				var section = section || 'about';
				LayoutController.showLayout('About', 'about', section, new Show.MainSectionView());
			}
		}
	});
	return Lvl.AboutApp.Show.Controller;
});