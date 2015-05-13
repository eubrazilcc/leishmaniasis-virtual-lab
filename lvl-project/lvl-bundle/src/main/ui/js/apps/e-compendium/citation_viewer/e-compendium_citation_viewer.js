/**
 * RequireJS module that defines the view: e-compendium->view_citation.
 */

define([ 'app', 'tpl!apps/e-compendium/citation_viewer/tpls/e-compendium_citation_viewer' ], function(Lvl, DisplayCitationTpl) {
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
					if (this['medlineCitation'] && this['medlineCitation'].article) {
						abbr = this['medlineCitation'].article.journal.isoabbreviation;
					}
					return abbr;
				},
				getIssue : function() {
					var issue = '';
					if (this['medlineCitation'] && this['medlineCitation'].article) {
						var objIssue = this['medlineCitation'].article.journal.journalIssue;
						var pubDate = objIssue.pubDate.yearOrMonthOrDayOrSeasonOrMedlineDate;
						var year = '', month = '', day = '', pages = '';
						for (var i = 0; i < pubDate.length && (year === '' || month === '' || day === ''); i++) {
							if (year === '') {
								year = pubDate[i].value;
							} else if (month === '') {
								month = pubDate[i].value;
							} else if (day === '') {
								day = pubDate[i].value;
							}
						}
						var pagination = this['medlineCitation'].article.paginationOrELocationID;
						for (var i = 0; i < pagination.length && pages === ''; i++) {
							if (pagination[i].startPageOrEndPageOrMedlinePgn) {
								var pagination2 = pagination[i].startPageOrEndPageOrMedlinePgn;
								for (var j = 0; j < pagination2.length && pages === ''; j++) {
									if (pagination2[j].value) {
										pages = pagination2[j].value;
									}
								}
							}
						}
						issue = year + ' ' + month + ' ' + day + '; ' + objIssue.issue + '(' + objIssue.volume + '):' + pages;
					}
					return issue;
				},
				getTitle : function() {
					var title = '';
					if (this['medlineCitation'] && this['medlineCitation'].article) {
						title = this['medlineCitation'].article.articleTitle;
					}
					return title;
				},
				getAuthors : function() {
					var authors = '';
					if (this['medlineCitation'] && this['medlineCitation'].article) {
						var authorList = this['medlineCitation'].article.authorList.author;
						for (var i = 0; i < authorList.length; i++) {
							var author = authorList[i].lastNameOrForeNameOrInitialsOrSuffixOrNameIDOrCollectiveName;
							var initials = '', lastname = '';
							for (var j = 0; j < author.length && (initials === '' || lastname === ''); j++) {
								if (lastname === '') {
									lastname = author[j].value;
								} else if (initials === '') {
									initials = author[j].value;
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
					if (this['medlineCitation'] && this['medlineCitation'].article) {
						citAbstract = this['medlineCitation'].article.abstract.abstractText;
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