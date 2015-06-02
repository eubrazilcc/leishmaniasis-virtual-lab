/**
 * RequireJS module that defines the entity: user.
 */

define([ 'app', 'backbone.paginator' ], function(Lvl) {
	Lvl.module('Entities.User', function(Entities, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Entities.User = Backbone.Model.extend({
			urlRoot : Lvl.config.get('auth.url') + '/users/',
			url : function() {
				return this.urlRoot + this.id + '?use_email=true';
			},
			idAttribute : 'email',
			defaults : {
				provider : '',
				userid : '',
				email : ''
			},
			initialize : function(options) {
				var selectable = new Backbone.Picky.Selectable(this);
				_.extend(this, selectable);
				this.oauth2_token = options.oauth2_token;
			},
			validate : function(attrs, options) {
				var errors = {};
				if (!attrs.provider) {
					errors.provider = 'can\'t be empty';
				}
				if (!attrs.userid) {
					errors.userid = 'can\'t be empty';
				}
				if (!attrs.email) {
					errors.email = 'can\'t be empty';
				}
				if (!_.isEmpty(errors)) {
					return errors;
				}
			}
		});
		Entities.UserPageableCollection = Backbone.PageableCollection.extend({
			model : Entities.User,
			mode : 'server',
			url : Lvl.config.get('auth.url') + '/users',
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
		Entities.UserAllCollection = Backbone.PageableCollection.extend({
			model : Entities.User,
			url : Lvl.config.get('auth.url') + '/users',
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
	return Lvl.Entities.User;
});