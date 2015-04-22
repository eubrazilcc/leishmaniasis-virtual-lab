/**
 * RequireJS module that defines the controller: open->about->show.
 */

define([ 'app', 'apps/open/layout/open_layout_ctrl', 'text!apps/open/about/show/tpls/about-main-section.html',
		'text!apps/open/about/show/tpls/about-project-section.html', 'apps/open/layout/entities/section' ], function(Lvl, LayoutController, MainSectionHtml,
		ProjectSectionHtml, SectionEntity) {
	Lvl.module('AboutApp.Show', function(Show, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Show.Controller = {
			showAbout : function(section) {
				var section = section || 'about';
				LayoutController.showLayout({
					application : 'about',
					activeSection : section,
					mainSection : new SectionEntity.Section({
						section : 'about',
						name : 'About',
						htmlContent : MainSectionHtml
					}),
					subsections : new SectionEntity.SectionCollection([ new SectionEntity.Section({
						id : 0,
						section : 'project',
						name : 'Project',
						htmlContent : ProjectSectionHtml
					}) ])
				});
			}
		}
	});
	return Lvl.AboutApp.Show.Controller;
});