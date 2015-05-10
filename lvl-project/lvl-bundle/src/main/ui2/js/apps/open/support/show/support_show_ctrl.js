/**
 * RequireJS module that defines the controller: open->support->show.
 */

define([ 'app', 'apps/open/layout/open_layout_ctrl', 'text!apps/open/support/show/tpls/support-main-section.html',
		'text!apps/open/support/show/tpls/support-mailing-list-section.html', 'text!apps/open/support/show/tpls/support-report-an-issue.html',
		'apps/open/layout/entities/section', 'apps/config/marionette/configuration', 'bootstrapvalidator', 'backbone.syphon' ], function(Lvl, LayoutController,
		MainSectionHtml, MailingListSectionHtml, ReportAnIssueSectionHtml, SectionEntity, Configuration) {
	Lvl.module('SupportApp.Show', function(Show, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var lvlService = new Configuration().get('service', '');
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
					subSections : new SectionEntity.SectionCollection([
							new SectionEntity.Section({
								id : 0,
								section : 'mailing-list',
								name : 'Mailing list',
								htmlContent : MailingListSectionHtml,
								events : {
									'focus #lvlSubscribeForm input.form-control' : function(e) {
										var form = $('#lvlSubscribeForm');
										if (Boolean(form.attr('data-pristine') === 'true')) {
											form.attr('data-pristine', 'false');
											form.on('init.form.bv', function(e, data) {
												data.bv.disableSubmitButtons(true);
											}).bootstrapValidator({
												submitButtons : 'button[type="submit"]',
												fields : {
													'email' : {
														verbose : false,
														validators : {
															notEmpty : {
																message : 'The email is required and cannot be empty'
															},
															emailAddress : {
																message : 'The input is not a valid email address'
															}
														}
													}
												}
											}).on('success.field.bv', function(e, data) {
												var isValid = data.bv.isValid();
												data.bv.disableSubmitButtons(!isValid);												
											});
										}
									},
									'click #lvlSubscribeBtn' : function(e) {
										e.preventDefault();
										$('#lvlSubscribeForm button[type="submit"]').attr('disabled', 'disabled');
										var formData = Backbone.Syphon.serialize(this);
										var requestData = {
											'email' : formData.email,
											'channels' : [ 'mailing list' ]
										};
										// submit request to LVL server
										var self = this;
										var jqxhr = $.ajax({
											type : 'POST',
											contentType : 'application/json',
											crossDomain : true,
											url : lvlService + '/support/subscriptions/requests',
											data : JSON.stringify(requestData)
										}).always(function() {
											var form = $('#lvlSubscribeForm');
											form.bootstrapValidator('resetForm', true);
											form.bootstrapValidator('disableSubmitButtons', true);
										}).done(
												function(data, textStatus, request) {
													require([ 'common/growl' ], function(createGrowl) {
														createGrowl('Subscription successful', 'You should receive a confirmation e-mail '
																+ 'at the address that you provided.', false);
													});
												}).fail(function(jqXHR, textStatus, errorThrown) {
											if (jqXHR.status !== 404) {
												require([ 'common/alert' ], function(alertDialog) {
													alertDialog('Error', 'The subscription request cannot be sent.');
												});
											}
										});
									}
								}
							}),
							new SectionEntity.Section({
								id : 1,
								section : 'report-an-issue',
								name : 'Report an issue',
								htmlContent : ReportAnIssueSectionHtml,
								events : {
									'focus #lvlIssueReportForm input.form-control' : function(e) {
										var form = $('#lvlIssueReportForm');
										if (Boolean(form.attr('data-pristine') === 'true')) {
											form.attr('data-pristine', 'false');
											form.on('init.form.bv', function(e, data) {
												data.bv.disableSubmitButtons(true);
											}).bootstrapValidator({
												submitButtons : 'button[type="submit"]',
												fields : {
													'email' : {
														verbose : false,
														validators : {
															notEmpty : {
																message : 'The email is required and cannot be empty'
															},
															emailAddress : {
																message : 'The input is not a valid email address'
															}
														}
													}

												// email, browser, system, config, steps, description, screenshot

												}
											}).on('success.field.bv', function(e, data) {
												var isValid = data.bv.isValid();
												data.bv.disableSubmitButtons(!isValid);
											});
										}
									},
									'click #lvlReportIssueBtn' : function(e) {
										e.preventDefault();
										$('#lvlIssueReportForm button[type="submit"]').attr('disabled', 'disabled');
										var formData = Backbone.Syphon.serialize(this);
										var requestData = {
											'email' : formData.email
										};
										// submit request to LVL server
										var self = this;
										var jqxhr = $.ajax({
											type : 'POST',
											contentType : 'application/json',
											crossDomain : true,
											url : lvlService + '/support',
											data : JSON.stringify(requestData)
										}).always(function() {
											var form = $('#lvlIssueReportForm');
											form.bootstrapValidator('resetForm', true);
											form.bootstrapValidator('disableSubmitButtons', true);
										}).done(
												function(data, textStatus, request) {
													require([ 'common/growl' ], function(createGrowl) {
														createGrowl('Subscription successful', 'You should receive a confirmation e-mail '
																+ 'at the address that you provided.', false);
													});
												}).fail(function(jqXHR, textStatus, errorThrown) {
											if (jqXHR.status !== 404) {
												require([ 'common/alert' ], function(alertDialog) {
													alertDialog('Error', 'The subscription request cannot be sent.');
												});
											}
										});
									}
								}
							}) ])
				});
			}
		}
	});
	return Lvl.SupportApp.Show.Controller;
});