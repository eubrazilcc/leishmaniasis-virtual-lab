/**
 * RequireJS module that defines the entity: reference.
 */

define([ 'app', 'apps/config/marionette/configuration', 'backbone.picky', 'backbone.paginator' ], function(Lvl, Configuration) {
	Lvl.module('Entities.Reference', function(Entities, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		Entities.Reference = Backbone.Model.extend({
			defaults : {
				pubmedId : '',
				publicationYear : '',
				seqids : [],
				title : ''
			},
			initialize : function() {
				var selectable = new Backbone.Picky.Selectable(this);
				_.extend(this, selectable);
			},
			validate : function(attrs, options) {
				var errors = {};
				if (!attrs.pubmedId) {
					errors.pubmedId = 'can\'t be empty';
				}
				if (!attrs.publicationYear) {
					errors.publicationYear = 'can\'t be empty';
				}
				if (!attrs.title) {
					errors.title = 'can\'t be empty';
				}
				if (!_.isEmpty(errors)) {
					return errors;
				}
			}
		});
		Entities.ReferenceCollection = Backbone.Collection.extend({
			model : Entities.Reference,
			comparator : 'pubmedId',
			initialize : function() {
				var singleSelect = new Backbone.Picky.SingleSelect(this);
				_.extend(this, singleSelect);
			}
		});
		Entities.ReferencePageableCollection = Backbone.PageableCollection.extend({
			model : Entities.Reference,
			mode : 'server',
			// url : 'references.json?burst=' + Math.random(),
			url : config.get('service', '') + '/references',
			initialize : function(options) {
				this.oauth2_token = options.oauth2_token
			},
			state : {
				pageSize : 100,
				firstPage : 0
			},
			queryParams : {
				totalPages : null,
				totalRecords : null,
				currentPage : 'page',
				pageSize : 'per_page',
				sortKey : 'sort',
				order : 'order'
			},
			parseState : function(resp, queryParams, state, options) {
				return {
					totalRecords : resp.totalCount
				};
			},
			parseRecords : function(resp, options) {
				return resp.elements;
			}
		});
	});
	return Lvl.Entities.Reference;
});