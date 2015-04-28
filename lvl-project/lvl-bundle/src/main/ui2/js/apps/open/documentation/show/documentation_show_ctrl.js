/**
 * RequireJS module that defines the controller: open->documentation->show.
 */

define([ 'app', 'apps/open/layout/open_layout_ctrl', 'text!apps/open/documentation/show/tpls/documentation-main-section.html',
		'text!apps/open/documentation/show/tpls/documentation-screencasts-section.html',
		'text!apps/open/documentation/show/tpls/documentation-presentations-section.html',
		'text!apps/open/documentation/show/tpls/documentation-publications-section.html', 'apps/open/layout/entities/section' ], function(Lvl,
		LayoutController, MainSectionHtml, ScreencastsSectionHtml, PresentationsSectionHtml, PublicationsSectionHtml, SectionEntity) {
	Lvl.module('DocumentationApp.Show', function(Show, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Show.Controller = {
			showDocumentation : function(section) {
				var section = section || 'doc';
				LayoutController.showLayout({
					activeSection : section,
					mainSection : new SectionEntity.Section({
						section : 'doc',
						name : 'Documentation',
						htmlContent : MainSectionHtml
					}),
					subSections : new SectionEntity.SectionCollection([ new SectionEntity.Section({
						id : 0,
						section : 'screencasts',
						name : 'Screencasts',
						htmlContent : ScreencastsSectionHtml,
						events : {
							'show.bs.modal #modalVideoViewer' : function(e) {
								var caller = $(e.relatedTarget);
								var data = caller.data('whatever');
								var modal = $('#modalVideoViewer');
								modal.find('.modal-title').text(data.title);
								modal.find('.modal-body div iframe').attr('src', data.url);
							}
						}
					}), new SectionEntity.Section({
						id : 1,
						section : 'presentations',
						name : 'Presentations',
						htmlContent : PresentationsSectionHtml
					}), new SectionEntity.Section({
						id : 2,
						section : 'publications',
						name : 'Publications',
						htmlContent : PublicationsSectionHtml
					}) ])
				});
			}
		}
	});
	return Lvl.DocumentationApp.Show.Controller;
});