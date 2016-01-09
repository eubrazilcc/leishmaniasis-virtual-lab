/**
 * RequireJS module that defines the entity: post.
 */

define([ 'app', 'backbone.picky', 'backbone.paginator' ], function(Lvl) {
	Lvl.module('Entities.Post', function(Entities, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Entities.Post = Backbone.Model.extend({			
			urlRoot : Lvl.config.get('service.url') + '/community/posts/~/',
			url : function() {
				return this.urlRoot + this.id;
			},
			defaults : {
				id : null,
				created : null,
				author : null,
				category : null,
				level : null,
				body : null
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
				if (!attrs.created) {
					errors.created = 'can\'t be empty';
				}
				if (!attrs.author) {
					errors.author = 'can\'t be empty';
				}
				if (!attrs.body) {
					errors.body = 'can\'t be empty';
				}				
				if (!_.isEmpty(errors)) {
					return errors;
				}
			}
		});
		Entities.PostCreate = Backbone.Model.extend({
			url : function() {
				return Lvl.config.get('service.url') + '/community/posts/~';
			}
		});
		Entities.PostCollection = Backbone.Collection.extend({
			model : Entities.Post,
			comparator : 'id',
			initialize : function() {
				var singleSelect = new Backbone.Picky.SingleSelect(this);
				_.extend(this, singleSelect);
			}
		});
		Entities.PostPageableCollection = Backbone.PageableCollection.extend({
			model : Entities.Post,
			mode : 'server',
			url : function() {
				return Lvl.config.get('service.url') + '/community/posts/~';
			},
			initialize : function(options) {
				this.oauth2_token = options.oauth2_token
			},
			state : {
				pageSize : 10,
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
				this.lvlOpHash = resp.hash;
				return {
					totalRecords : resp.totalCount || 0
				};
			},
			parseRecords : function(resp, options) {				
				return resp.elements;
			}
		});
	});
	return Lvl.Entities.Post;
});