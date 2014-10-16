/**
 * RequireJS module that defines the entity: sequence.
 */

define([ 'app', 'apps/config/marionette/configuration', 'backbone.picky', 'backbone.paginator' ], function(Lvl, Configuration) {
	Lvl.module('Entities.Sequence', function(Entities, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
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
			// url : 'sequences.json?burst=' + Math.random(),
			url : config.get('service', '') + '/sandflies',
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
	return Lvl.Entities.Sequence;
});