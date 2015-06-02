/**
 * RequireJS module that defines the entity: link.
 */

define([ 'app', 'backbone.paginator' ], function(Lvl) {
	Lvl.module('Entities.Link', function(Entities, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Entities.Link = Backbone.Model.extend({
			urlRoot : Lvl.config.get('service.url') + '/datasets/open_access/~/',
			idAttribute : 'filename',
			defaults : {
				openAccessLink : '',
				openAccessDate : ''
			},
			initialize : function() {
				var selectable = new Backbone.Picky.Selectable(this);
				_.extend(this, selectable);
			},
			validate : function(attrs, options) {
				var errors = {};
				if (!attrs.downloadUri) {
					errors.downloadUri = 'can\'t be empty';
				}
				if (!attrs.mime) {
					errors.mime = 'can\'t be empty';
				}
				if (!attrs.path) {
					errors.path = 'can\'t be empty';
				}
				if (!_.isEmpty(errors)) {
					return errors;
				}
			}
		});
		Entities.LinkCreate = Backbone.Model.extend({
			urlRoot : Lvl.config.get('service.url') + '/datasets/open_access/~/'
		});
		Entities.LinkPageableCollection = Backbone.PageableCollection.extend({
			model : Entities.Link,
			mode : 'server',
			url : Lvl.config.get('service.url') + '/datasets/open_access/~',
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
		Entities.LinkAllCollection = Backbone.PageableCollection.extend({
			model : Entities.Link,
			url : Lvl.config.get('service.url') + '/datasets/open_access/~',
			initialize : function(options) {
				this.oauth2_token = options.oauth2_token
			},
			state : {
				pageSize : 1000000,
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
	return Lvl.Entities.Link;
});