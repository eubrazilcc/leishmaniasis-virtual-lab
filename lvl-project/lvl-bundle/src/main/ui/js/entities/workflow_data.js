/**
 * RequireJS module that defines the entity: workflow_data.
 */

define([ 'app', 'apps/config/marionette/configuration', 'backbone.picky', 'backbone.paginator' ], function(Lvl, Configuration) {
	Lvl.module('Entities.WorkflowData', function(Entities, Lvl, Backbone, Marionette, $, _) {
		'use strict';
		var config = new Configuration();
		Entities.WorkflowData = Backbone.Model.extend({
			urlRoot : config.get('service', '') + '/pipelines_data/',
			defaults : {
				id : '',
				name : '',
				description : '',
				created : ''
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
				if (!attrs.name) {
					errors.name = 'can\'t be empty';
				}
				if (!attrs.created) {
					errors.created = 'can\'t be empty';
				}
				if (!_.isEmpty(errors)) {
					return errors;
				}
			}
		});
		Entities.WorkflowDataCollection = Backbone.Collection.extend({
			model : Entities.WorkflowData,
			comparator : 'id',
			initialize : function() {
				var singleSelect = new Backbone.Picky.SingleSelect(this);
				_.extend(this, singleSelect);
			}
		});
		Entities.WorkflowDataPageableCollection = Backbone.PageableCollection.extend({
			model : Entities.WorkflowData,
			mode : 'server',
			// url : 'wrokflow_data.json?burst=' + Math.random(),
			url : config.get('service', '') + '/pipelines_data',
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
		Entities.WorkflowDataAllCollection = Backbone.PageableCollection.extend({
			model : Entities.WorkflowData,
			url : config.get('service', '') + '/pipelines_data',
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
	return Lvl.Entities.WorkflowData;
});