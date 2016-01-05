/**
 * RequireJS module that defines the entity: pending sequence.
 */

define([ 'app', 'backbone.picky', 'backbone.paginator' ], function(Lvl) {
	Lvl.module('Entities.PendingSequence', function(Entities, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Entities.PendingSequence = Backbone.Model.extend({			
			urlRoot : Lvl.config.get('service.url') + '/pending/',
			url : function() {
				return this.urlRoot + this.get('dataSource') + '/~/' + this.id;
			},
			defaults : {
				sample : null,
				sequence : '',
				preparation : null
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
			url : function() {
				return Lvl.config.get('service.url') + '/pending/' + this.data_source + '/~';
			}
		});
		Entities.PendingSequenceCollection = Backbone.Collection.extend({
			model : Entities.PendingSequence,
			comparator : 'id',
			initialize : function() {
				var singleSelect = new Backbone.Picky.SingleSelect(this);
				_.extend(this, singleSelect);
			}
		});
		Entities.PendingSequencePageableCollection = Backbone.PageableCollection.extend({
			model : Entities.PendingSequence,
			mode : 'server',
			url : function() {
				return Lvl.config.get('service.url') + '/pending/' + this.data_source + '/~';
			},
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
	return Lvl.Entities.PendingSequence;
});