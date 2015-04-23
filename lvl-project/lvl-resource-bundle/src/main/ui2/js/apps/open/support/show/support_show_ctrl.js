/**
 * RequireJS module that defines the controller: open->support->show.
 */

define([ 'app', 'apps/open/layout/open_layout_ctrl', 'text!apps/open/support/show/tpls/support-main-section.html',
		'text!apps/open/support/show/tpls/support-mailing-list-section.html', 'text!apps/open/support/show/tpls/support-report-an-issue.html',
		'apps/open/layout/entities/section' ],
		function(Lvl, LayoutController, MainSectionHtml, MailingListSectionHtml, ReportAnIssueSectionHtml, SectionEntity) {
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
							}), new SectionEntity.Section({
								id : 1,
								section : 'report-an-issue',
								name : 'Report an issue',
								htmlContent : ReportAnIssueSectionHtml
							}) ])
						});
					}
				}
			});
			return Lvl.SupportApp.Show.Controller;
		});