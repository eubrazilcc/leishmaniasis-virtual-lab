/**
 * RequireJS module that defines the entity: issue.
 */

define([ 'app', 'apps/config/marionette/configuration', 'backbone.picky', 'backbone.paginator' ], function(Lvl, Configuration) {
	Lvl.module('Entities.Issue', function(Entities, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		Entities.Issue = Backbone.Model.extend({
			urlRoot : config.get('service', '') + '/support/issues',
			defaults : {
				id : '',
				email : '',
				opened : '',
				browser : '',
				system : '',
				description : '',
				screenshot : '',
				status : '',
				owner : '',
				closed : ''
			},
			initialize : function() {
				var selectable = new Backbone.Picky.Selectable(this);
				_.extend(this, selectable);
			},
			validate : function(attrs, options) {
				var errors = {};
				if (!attrs.id) {
					errors.id = 'can\'t be empty';
				}
				if (!attrs.email) {
					errors.email = 'can\'t be empty';
				}
				if (!attrs.requested) {
					errors.requested = 'can\'t be empty';
				}
				if (!attrs.channels) {
					errors.channels = 'can\'t be empty';
				}
				if (!_.isEmpty(errors)) {
					return errors;
				}
			}
		});
		Entities.IssueCollection = Backbone.Collection.extend({
			model : Entities.Issue,
			comparator : 'id',
			initialize : function() {
				var singleSelect = new Backbone.Picky.SingleSelect(this);
				_.extend(this, singleSelect);
			}
		});
		Entities.IssuePageableCollection = Backbone.PageableCollection.extend({
			model : Entities.Issue,
			mode : 'server',
			url : config.get('service', '') + '/support/issues',
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
	return Lvl.Entities.Issue;
});