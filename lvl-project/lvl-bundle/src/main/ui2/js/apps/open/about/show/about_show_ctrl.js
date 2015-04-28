/**
 * RequireJS module that defines the controller: open->about->show.
 */

define([ 'app', 'apps/open/layout/open_layout_ctrl', 'text!apps/open/about/show/tpls/about-main-section.html',
		'text!apps/open/about/show/tpls/about-project-section.html', 'text!apps/open/about/show/tpls/about-key-featurest-section.html',
		'apps/open/layout/entities/section' ], function(Lvl, LayoutController, MainSectionHtml, ProjectSectionHtml, KeyFeaturesSectionHtml, SectionEntity) {
	Lvl.module('AboutApp.Show', function(Show, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Show.Controller = {
			showAbout : function(section) {
				var section = section || 'about';
				LayoutController.showLayout({
					activeSection : section,
					mainSection : new SectionEntity.Section({
						section : 'about',
						name : 'About',
						htmlContent : MainSectionHtml
					}),
					subSections : new SectionEntity.SectionCollection([ new SectionEntity.Section({
						id : 0,
						section : 'project',
						name : 'Project',
						htmlContent : ProjectSectionHtml
					}), new SectionEntity.Section({
						id : 1,
						section : 'key-features',
						name : 'Key features',
						htmlContent : KeyFeaturesSectionHtml
					}) ])
				});
			}
		}
	});
	return Lvl.AboutApp.Show.Controller;
});