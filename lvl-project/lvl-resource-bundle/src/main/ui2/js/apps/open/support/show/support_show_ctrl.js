/**
 * RequireJS module that defines the controller: open->support->show.
 */

define([ 'app', 'apps/open/layout/open_layout_ctrl', 'text!apps/open/support/show/tpls/support-main-section.html',
		'text!apps/open/support/show/tpls/support-mailing-list-section.html', 'apps/open/layout/entities/section' ], function(Lvl, LayoutController,
		MainSectionHtml, MailingListSectionHtml, SectionEntity) {
	Lvl.module('SupportApp.Show', function(Show, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Show.Controller = {
			showSupport : function(section) {
				var section = section || 'support';
				LayoutController.showLayout({
					activeSection : section,
					mainSection : new SectionEntity.Section({
						section : 'support',
						name : 'Support',
						htmlContent : MainSectionHtml
					}),
					subSections : new SectionEntity.SectionCollection([ new SectionEntity.Section({
						id : 0,
						section : 'mailing-list',
						name : 'Mailing list',
						htmlContent : MailingListSectionHtml
					}) ])
				});
			}
		}
	});
	return Lvl.SupportApp.Show.Controller;
});