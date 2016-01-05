/**
 * RequireJS module that defines the entity: sample.
 */

define([ 'app', 'backbone.picky', 'backbone.paginator' ], function(Lvl) {
	Lvl.module('Entities.Sample', function(Entities, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		Entities.Sample = Backbone.Model.extend({			
			urlRoot : Lvl.config.get('service.url') + '/samples/',
			url : function() {
				return this.urlRoot + this.get('dataSource') + '/' + this.id + '/export/dwc/xml';
			},
			defaults : {
				id : '',
				collectionId : '',
				catalogNumber : ''
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
				if (!attrs.collectionId) {
					errors.collectionId = 'can\'t be empty';
				}
				if (!attrs.catalogNumber) {
					errors.catalogNumber = 'can\'t be empty';
				}				
				if (!_.isEmpty(errors)) {
					return errors;
				}
			}
		});
		Entities.SampleCollection = Backbone.Collection.extend({
			model : Entities.Sample,
			comparator : 'id',
			initialize : function() {
				var singleSelect = new Backbone.Picky.SingleSelect(this);
				_.extend(this, singleSelect);
			}
		});
		Entities.SamplePageableCollection = Backbone.PageableCollection.extend({
			model : Entities.Sample,
			mode : 'server',
			url : function() {
				return Lvl.config.get('service.url') + '/samples/' + this.data_source;
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
	return Lvl.Entities.Sample;
});