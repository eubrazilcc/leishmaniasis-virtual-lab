/**
 * RequireJS module that defines the controller: open->software->show.
 */

define([ 'app', 'apps/open/layout/open_layout_ctrl', 'text!apps/open/software/show/tpls/software-main-section.html',
		'text!apps/open/software/show/tpls/software-releases-section.html', 'text!apps/open/software/show/tpls/software-downloads-section.html',
		'text!apps/open/software/show/tpls/software-development-section.html', 'apps/open/layout/entities/section' ], function(Lvl, LayoutController,
		MainSectionHtml, ReleasesSectionHtml, DownloadsSectionHtml, DevelopmentSectionHtml, SectionEntity) {
	Lvl.module('SoftwareApp.Show', function(Show, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Show.Controller = {
			showSoftware : function(section) {
				var section = section || 'software';
				LayoutController.showLayout({
					activeSection : section,
					mainSection : new SectionEntity.Section({
						section : 'software',
						name : 'Software',
						htmlContent : MainSectionHtml
					}),
					subSections : new SectionEntity.SectionCollection([ new SectionEntity.Section({
						id : 0,
						section : 'releases',
						name : 'Releases',
						htmlContent : ReleasesSectionHtml
					}), new SectionEntity.Section({
						id : 1,
						section : 'downloads',
						name : 'Downloads',
						htmlContent : DownloadsSectionHtml
					}), new SectionEntity.Section({
						id : 2,
						section : 'development',
						name : 'Development',
						htmlContent : DevelopmentSectionHtml
					}) ])
				});
			}
		}
	});
	return Lvl.SoftwareApp.Show.Controller;
});