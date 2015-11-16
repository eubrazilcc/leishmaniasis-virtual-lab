/**
 * RequireJS module that defines the entity: sequence.
 */

define([ 'app', 'backbone.picky', 'backbone.paginator' ], function(Lvl) {
	Lvl.module('Entities.Sequence', function(Entities, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Entities.Sequence = Backbone.Model.extend({
			defaults : {
				id : '',
				dataSource : '',
				definition : '',
				accession : '',
				version : '',
				organism : '',
				length : 0
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
				if (!attrs.dataSource) {
					errors.dataSource = 'can\'t be empty';
				}
				if (!attrs.accession) {
					errors.accession = 'can\'t be empty';
				}
				if (!attrs.version) {
					errors.version = 'can\'t be empty';
				}
				if (!attrs.organism) {
					errors.organism = 'can\'t be empty';
				}
				if (!attrs.length) {
					errors.length = 'can\'t be empty';
				}
				if (!_.isEmpty(errors)) {
					return errors;
				}
			}
		});
		Entities.SequenceCollection = Backbone.Collection.extend({
			model : Entities.Sequence,
			comparator : 'id',
			initialize : function() {
				var singleSelect = new Backbone.Picky.SingleSelect(this);
				_.extend(this, singleSelect);
			}
		});
		Entities.SequencePageableCollection = Backbone.PageableCollection.extend({
			model : Entities.Sequence,
			mode : 'server',
			url : function() {
				return Lvl.config.get('service.url') + '/sequences/' + this.data_source;
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
					totalRecords : resp.totalCount
				};
			},
			parseRecords : function(resp, options) {
				return resp.elements;
			}
		});
	});
	return Lvl.Entities.Sequence;
});