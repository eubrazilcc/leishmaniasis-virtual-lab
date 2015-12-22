/**
 * RequireJS module that defines the entity: pending sequence.
 */

define([ 'app', 'backbone.paginator' ], function(Lvl) {
	Lvl.module('Entities.PendingSequence', function(Entities, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Entities.PendingSequence = Backbone.Model.extend({
			urlRoot : Lvl.config.get('service.url') + '/pending/sequences/~/',			
			defaults : {
				sample : null,
				sequence : ''
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
				if (!attrs.sample) {
					errors.sample = 'can\'t be empty';
				}
				if (!attrs.sequence) {
					errors.sequence = 'can\'t be empty';
				}
				if (!_.isEmpty(errors)) {
					return errors;
				}
			}
		});
		Entities.PendingSequenceCreate = Backbone.Model.extend({
			urlRoot : Lvl.config.get('service.url') + '/pending/sequences/~/'			
		});
		Entities.PendingSequencePageableCollection = Backbone.PageableCollection.extend({
			model : Entities.PendingSequence,
			mode : 'server',
			url : Lvl.config.get('service.url') + '/pending/sequences/~',
			initialize : function(options) {
				this.oauth2_token = options.oauth2_token, this.data_source = options.data_source || 'sandflies'
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
		Entities.PendingSequenceAllCollection = Backbone.PageableCollection.extend({
			model : Entities.PendingSequence,
			url : Lvl.config.get('service.url') + '/pending/sequences/~',
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
	return Lvl.Entities.PendingSequence;
});