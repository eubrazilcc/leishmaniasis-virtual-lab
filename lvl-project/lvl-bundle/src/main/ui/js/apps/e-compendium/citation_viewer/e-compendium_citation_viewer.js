/**
 * RequireJS module that defines the view: e-compendium->view_citation.
 */

define([ 'app', 'tpl!apps/e-compendium/citation_viewer/templates/e-compendium_citation_viewer' ], function(Lvl, DisplayCitationTpl) {
	Lvl.module('ECompendiumApp.Citation.View', function(View, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		View.Content = Marionette.ItemView.extend({
			template : DisplayCitationTpl,
			templateHelpers : {
				gbField : function(id, encode) {
					var encode = encode || false;
					return encode ? encodeURIComponent(this[id]) : this[id];
				},
				getISOAbbreviation : function() {
					var abbr = '';
					if (this['MedlineCitation'] && this['MedlineCitation'].Article) {
						abbr = this['MedlineCitation'].Article.Journal.ISOAbbreviation;
					}
					return abbr;
				},
				getIssue : function() {
					var issue = '';
					if (this['MedlineCitation'] && this['MedlineCitation'].Article) {
						var objIssue = this['MedlineCitation'].Article.Journal.JournalIssue;
						var pubDate = objIssue.PubDate.yearOrMonthOrDayOrSeasonOrMedlineDate;
						var year = '', month = '', pages = '';
						for (var i = 0; i < pubDate.length && (year === '' || month === ''); i++) {
							if (pubDate[i].Year) {
								year = pubDate[i].Year.value;
							} else if (pubDate[i].Month) {
								month = pubDate[i].Month.value;
							}
						}
						var pagination = this['MedlineCitation'].Article.paginationOrELocationID;
						for (var i = 0; i < pagination.length && pages === ''; i++) {
							if (pagination[i].Pagination) {
								var pagination2 = pagination[i].Pagination.startPageOrEndPageOrMedlinePgn;
								for (var j = 0; j < pagination2.length && pages === ''; j++) {
									if (pagination2[j].MedlinePgn) {
										pages = pagination2[j].MedlinePgn.value;
									}
								}
							}
						}
						issue = year + ' ' + month + ' ;' + objIssue.Issue + '(' + objIssue.Volume + '):' + pages;
					}
					return issue;
				},
				getTitle : function() {
					var title = '';
					if (this['MedlineCitation'] && this['MedlineCitation'].Article) {
						title = this['MedlineCitation'].Article.ArticleTitle;
					}
					return title;
				},
				getAuthors : function() {
					var authors = '';
					if (this['MedlineCitation'] && this['MedlineCitation'].Article) {
						var authorList = this['MedlineCitation'].Article.AuthorList.Author;
						for (var i = 0; i < authorList.length; i++) {
							var author = authorList[i].lastNameOrForeNameOrInitialsOrSuffixOrNameIDOrCollectiveName;
							var initials = '', lastname = '';
							for (var j = 0; j < author.length && (initials === '' || lastname === ''); j++) {
								if (author[j].LastName) {
									lastname = author[j].LastName.value;
								} else if (author[j].Initials) {
									initials = author[j].Initials.value;
								}
							}
							if (initials !== '' || lastname !== '') {
								authors += lastname + ' ' + initials + ', ';
							}
						}
					}
					return authors.length >= 2 ? authors.substring(0, authors.length - 2) : '';
				},
				getAbstract : function() {
					var citAbstract = '';
					if (this['MedlineCitation'] && this['MedlineCitation'].Article) {
						citAbstract = this['MedlineCitation'].Article.Abstract.AbstractText;
					}
					return citAbstract;
				}
			},
			initialize : function() {
				this.listenTo(this.model, 'change', this.render);
				var self = this;
				self.model.fetch({
					reset : true
				});
			}
		});
	});
	return Lvl.ECompendiumApp.Citation.View;
});