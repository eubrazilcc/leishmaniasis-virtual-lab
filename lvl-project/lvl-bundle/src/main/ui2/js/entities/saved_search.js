/**
 * RequireJS module that defines the entity: saved_search.
 */

define([ 'app', 'apps/config/marionette/configuration', 'backbone.picky', 'backbone.paginator' ], function(Lvl, Configuration) {
	Lvl.module('Entities.SavedSearch', function(Entities, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		Entities.SavedSearch = Backbone.Model.extend({
			urlRoot : config.get('service', '') + '/saved/searches',
			defaults : {
				type : '',
				saved : '',
				description : '',
				search : {}
			},
			initialize : function() {
				var selectable = new Backbone.Picky.Selectable(this);
				_.extend(this, selectable);
			},
			validate : function(attrs, options) {
				var errors = {};				
				if (!attrs.type) {
					errors.type = 'can\'t be empty';
				}
				if (!attrs.search) {
					errors.search = 'can\'t be empty';
				}
				if (!_.isEmpty(errors)) {
					return errors;
				}
			}
		});
		Entities.SavedSearchCollection = Backbone.Collection.extend({
			model : Entities.SavedSearch,
			comparator : 'searchId',
			initialize : function() {
				var singleSelect = new Backbone.Picky.SingleSelect(this);
				_.extend(this, singleSelect);
			}
		});
		Entities.SavedSearchPageableCollection = Backbone.PageableCollection.extend({
			model : Entities.SavedSearch,
			mode : 'server',
			url : function() {
				return config.get('service', '') + '/saved/searches';
			},
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
				// set additional properties before returning to caller
				this.formattedQuery = resp.formattedQuery;
				return {
					totalRecords : resp.totalCount
				};
			},
			parseRecords : function(resp, options) {
				return resp.elements;
			}
		});
	});
	return Lvl.Entities.SavedSearch;
});